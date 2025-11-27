package com.skillrat.user.service.impl;

import com.skillrat.user.domain.LeaveStatus;
import com.skillrat.user.dto.LeaveDTO;
import com.skillrat.user.repo.EmployeeLeaveDao;
import com.skillrat.user.domain.EmployeeLeave;
import com.skillrat.user.service.LeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final EmployeeLeaveDao leaveDao;

    public LeaveServiceImpl(EmployeeLeaveDao leaveDao) {
        this.leaveDao = leaveDao;
    }

    @Override
    public LeaveDTO apply(LeaveDTO req) {
        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new IllegalArgumentException("startDate must be on/before endDate");
        }
        com.skillrat.user.domain.Employee empRef = new com.skillrat.user.domain.Employee();
        empRef.setId(req.getEmployeeId());
        EmployeeLeave el = EmployeeLeave.builder()
                .employee(empRef)
                .leaveType(req.getLeaveType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(LeaveStatus.PENDING)
                .build();
        el = leaveDao.save(el);
        return toResp(el);
    }

    @Override
    public LeaveDTO approve(UUID id) {
        EmployeeLeave el = leaveDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        if (el.getStatus() == LeaveStatus.REJECTED) {
            throw new IllegalStateException("Cannot approve a rejected leave");
        }
        el.setStatus(LeaveStatus.APPROVED);
        el = leaveDao.save(el);
        return toResp(el);
    }

    @Override
    public LeaveDTO reject(UUID id) {
        EmployeeLeave el = leaveDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        if (el.getStatus() == LeaveStatus.APPROVED) {
            throw new IllegalStateException("Cannot reject an approved leave");
        }
        el.setStatus(LeaveStatus.REJECTED);
        el = leaveDao.save(el);
        return toResp(el);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDTO> listByEmployee(UUID employeeId) {
        return leaveDao.findByEmployee_Id(employeeId).stream().map(this::toResp).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDTO> listApprovedOverlapping(UUID employeeId, LocalDate from, LocalDate to) {
        return leaveDao
                .findByEmployee_IdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId, LeaveStatus.APPROVED, to, from)
                .stream()
                .map(this::toResp)
                .collect(Collectors.toList());
    }

    private LeaveDTO toResp(EmployeeLeave el) {
        return LeaveDTO.builder()
                .id(el.getId())
                .employeeId(el.getEmployee() != null ? el.getEmployee().getId() : null)
                .leaveType(el.getLeaveType())
                .startDate(el.getStartDate())
                .endDate(el.getEndDate())
                .status(el.getStatus())
                .build();
    }
}
