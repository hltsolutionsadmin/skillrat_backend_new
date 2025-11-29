package com.skillrat.user.service.impl;

import com.skillrat.user.dto.ComponentDTO;
import com.skillrat.user.dto.PayslipDTO;
import com.skillrat.user.domain.EmployeePayslip;
import com.skillrat.user.domain.EmployeePayslipComponent;
import com.skillrat.user.domain.EmployeeSalaryComponent;
import com.skillrat.user.domain.EmployeeSalaryStructure;
import com.skillrat.user.domain.SalaryComponent;
import com.skillrat.user.domain.EmployeeLeaveDeduction;
import com.skillrat.user.domain.User;
import com.skillrat.user.service.PayrollService;
import com.skillrat.user.domain.LeaveType;
import com.skillrat.user.client.ProjectLeaveClient;
import com.skillrat.user.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final EmployeeSalaryStructureDao structureDao;
    private final EmployeeSalaryComponentDao structureCompDao;
    private final SalaryComponentDao componentDao;
    private final ProjectLeaveClient projectLeaveClient;
    private final EmployeePayslipDao payslipDao;
    private final EmployeePayslipComponentDao payslipComponentDao;
    private final EmployeeLeaveDeductionDao leaveDeductionDao;
    private final UserRepository userRepository;

    public PayrollServiceImpl(EmployeeSalaryStructureDao structureDao,
                              EmployeeSalaryComponentDao structureCompDao,
                              SalaryComponentDao componentDao,
                              ProjectLeaveClient projectLeaveClient,
                              EmployeePayslipDao payslipDao,
                              EmployeePayslipComponentDao payslipComponentDao,
                              EmployeeLeaveDeductionDao leaveDeductionDao,
                              UserRepository userRepository) {
        this.structureDao = structureDao;
        this.structureCompDao = structureCompDao;
        this.componentDao = componentDao;
        this.projectLeaveClient = projectLeaveClient;
        this.payslipDao = payslipDao;
        this.payslipComponentDao = payslipComponentDao;
        this.leaveDeductionDao = leaveDeductionDao;
        this.userRepository = userRepository;
    }

    @SuppressWarnings("null")
	@Override
    public PayslipDTO generate(PayslipDTO req) {
        int month = req.getMonth();
        int year = req.getYear();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        // Check if already generated
        Optional<EmployeePayslip> existing = payslipDao.findByEmployee_IdAndMonthAndYear(req.getEmployeeId(), month, year);
        if (existing.isPresent()) {
            return toDto(existing.get(), payslipComponentDao.findByPayslip_Id(existing.get().getId()));
        }

        // 1) Fetch latest structure effective on or before month end
        EmployeeSalaryStructure structure = structureDao
                .findFirstByEmployee_IdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(req.getEmployeeId(), to)
                .orElseThrow(() -> new IllegalStateException("No salary structure found for employee"));

        // Components of structure
        List<EmployeeSalaryComponent> comps = structureCompDao.findBySalaryStructure_Id(structure.getId());

        // 2) Separate earnings/deductions
        BigDecimal earnings = BigDecimal.ZERO;
        BigDecimal fixedDeductions = BigDecimal.ZERO;
        List<EmployeePayslipComponent> payslipComponents = new ArrayList<>();
        for (EmployeeSalaryComponent c : comps) {
            SalaryComponent sc = componentDao.findById(c.getComponent().getId())
                    .orElseThrow(() -> new IllegalStateException("Missing component " + c.getComponent().getId()));
            if (sc.getType() == SalaryComponent.ComponentType.EARNING) {
                earnings = earnings.add(c.getAmount());
            } else {
                fixedDeductions = fixedDeductions.add(c.getAmount());
            }
            payslipComponents.add(EmployeePayslipComponent.builder()
                    .componentName(sc.getName())
                    .componentType(sc.getType())
                    .amount(c.getAmount())
                    .build());
        }

        // 3) Attendance (not used directly in formula per spec) and 4) Approved LOP leaves
        // LOP = Approved UNPAID leaves overlapping the month (via project-service Feign client)
        List<ProjectLeaveClient.ApprovedLeaveDTO> approvedLeaves = projectLeaveClient
                .getApprovedLeaves(req.getEmployeeId(), month, year);
        long lopDays = 0L;
        for (ProjectLeaveClient.ApprovedLeaveDTO l : approvedLeaves) {
            // project-service returns only APPROVED; filter UNPAID type
            LeaveType ltype;
            try { ltype = LeaveType.valueOf(l.leaveType); } catch (Exception ex) { continue; }
            if (ltype != LeaveType.UNPAID) continue;
            LocalDate s = l.startDate.isBefore(from) ? from : l.startDate;
            LocalDate e = l.endDate.isAfter(to) ? to : l.endDate;
            lopDays += (e.toEpochDay() - s.toEpochDay()) + 1;
        }

        // 5) Calculate leave deduction
        int totalWorkingDays = workingDaysInMonth(ym); // simple calc: Mon-Fri as working days
        if (totalWorkingDays <= 0) totalWorkingDays = ym.lengthOfMonth();
        BigDecimal dailySalary = structure.getGrossSalary()
                .divide(BigDecimal.valueOf(totalWorkingDays), 2, RoundingMode.HALF_UP);
        BigDecimal leaveDeduction = dailySalary.multiply(BigDecimal.valueOf(lopDays)).setScale(2, RoundingMode.HALF_UP);

        // 6) Totals
        BigDecimal totalEarnings = earnings.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDeductions = fixedDeductions.add(leaveDeduction).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netSalary = totalEarnings.subtract(totalDeductions).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        // 7) Store payslip
        com.skillrat.user.domain.Employee empRef = new com.skillrat.user.domain.Employee();
        empRef.setId(req.getEmployeeId());
        EmployeePayslip payslip = EmployeePayslip.builder()
                .employee(empRef)
                .month(month)
                .year(year)
                .totalEarnings(totalEarnings)
                .totalDeductions(totalDeductions)
                .unpaidLeaveDeduction(leaveDeduction)
                .netSalary(netSalary)
                .build();
        payslip = payslipDao.save(payslip);

        // Persist monthly leave deduction summary
        UUID b2bUnitId = null;
        Optional<User> empUser = userRepository.findById(req.getEmployeeId());
        if (empUser.isPresent()) {
            b2bUnitId = (empUser.get().getB2bUnit() != null) ? empUser.get().getB2bUnit().getId() : null;
        }
        EmployeeLeaveDeduction eld = new EmployeeLeaveDeduction();
        eld.setEmployee(empRef);
        if (b2bUnitId != null) {
            com.skillrat.user.organisation.domain.B2BUnit bu = new com.skillrat.user.organisation.domain.B2BUnit();
            bu.setId(b2bUnitId);
            eld.setB2bUnit(bu);
        }
        eld.setYear(year);
        eld.setMonth(month);
        eld.setUnpaidLeaveDays(BigDecimal.valueOf(lopDays));
        eld.setTotalDeduction(leaveDeduction);
        leaveDeductionDao.save(eld);

        // 8) Store components
        for (EmployeePayslipComponent pc : payslipComponents) {
            pc.setPayslip(payslip);
            payslipComponentDao.save(pc);
        }

        return toDto(payslip, payslipComponents);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PayslipDTO> get(UUID employeeId, int month, int year) {
        return payslipDao.findByEmployee_IdAndMonthAndYear(employeeId, month, year)
                .map(p -> toDto(p, payslipComponentDao.findByPayslip_Id(p.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayslipDTO> listByEmployee(UUID employeeId) {
        return payslipDao.findByEmployee_IdOrderByYearDescMonthDesc(employeeId).stream()
                .map(p -> toDto(p, payslipComponentDao.findByPayslip_Id(p.getId())))
                .collect(Collectors.toList());
    }

    private PayslipDTO toDto(EmployeePayslip p, List<EmployeePayslipComponent> comps) {
        return PayslipDTO.builder()
                .id(p.getId())
                .employeeId(p.getEmployee() != null ? p.getEmployee().getId() : null)
                .month(p.getMonth())
                .year(p.getYear())
                .totalEarnings(p.getTotalEarnings())
                .totalDeductions(p.getTotalDeductions())
                .unpaidLeaveDeduction(p.getUnpaidLeaveDeduction())
                .netSalary(p.getNetSalary())
                .generatedAt(p.getGeneratedAt())
                .components(
                        comps.stream()
                                .map(c -> ComponentDTO.builder()
                                        .name(c.getComponentName())
                                        .type(c.getComponentType())
                                        .amount(c.getAmount())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }


    private int workingDaysInMonth(YearMonth ym) {
        int days = 0;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            var dow = ym.atDay(d).getDayOfWeek();
            switch (dow) {
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> days++;
                default -> {}
            }
        }
        return days;
    }
}
