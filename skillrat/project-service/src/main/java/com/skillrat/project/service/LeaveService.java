package com.skillrat.project.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.domain.LeaveBalance;
import com.skillrat.project.domain.LeaveRequest;
import com.skillrat.project.domain.LeaveStatus;
import com.skillrat.project.domain.LeaveType;
import com.skillrat.project.domain.Project;
import com.skillrat.project.domain.ProjectMember;
import com.skillrat.project.domain.TimeEntry;
import com.skillrat.project.domain.TimeEntryStatus;
import com.skillrat.project.domain.TimeEntryType;
import com.skillrat.project.domain.WBSAllocation;
import com.skillrat.project.domain.WBSCategory;
import com.skillrat.project.domain.WBSElement;
import com.skillrat.project.repo.LeaveBalanceRepository;
import com.skillrat.project.repo.LeaveRequestRepository;
import com.skillrat.project.repo.ProjectMemberRepository;
import com.skillrat.project.repo.TimeEntryRepository;
import com.skillrat.project.repo.WBSAllocationRepository;
import com.skillrat.project.repo.WBSElementRepository;

@Service
public class LeaveService {

    private final LeaveRequestRepository requestRepository;
    private final LeaveBalanceRepository balanceRepository;
    private final ProjectMemberRepository memberRepository;
    private final WBSElementRepository wbsRepository;
    private final WBSAllocationRepository allocationRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final HolidayService holidayService;

    public LeaveService(LeaveRequestRepository requestRepository,
                        LeaveBalanceRepository balanceRepository,
                        ProjectMemberRepository memberRepository,
                        WBSElementRepository wbsRepository,
                        WBSAllocationRepository allocationRepository,
                        TimeEntryRepository timeEntryRepository,
                        HolidayService holidayService) {
        this.requestRepository = requestRepository;
        this.balanceRepository = balanceRepository;
        this.memberRepository = memberRepository;
        this.wbsRepository = wbsRepository;
        this.allocationRepository = allocationRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.holidayService = holidayService;
    }

    @Transactional
    public LeaveRequest request(UUID employeeId, UUID b2bUnitId, LeaveType type, LocalDate from, LocalDate to, BigDecimal perDayHours, String note) {
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(employeeId);
        lr.setB2bUnitId(b2bUnitId);
        lr.setType(type == null ? LeaveType.OTHER : type);
        lr.setFromDate(from);
        lr.setToDate(to);
        lr.setPerDayHours(perDayHours == null ? new BigDecimal("8.00") : perDayHours);
        lr.setStatus(LeaveStatus.REQUESTED);
        lr.setNote(note);
        lr.setTenantId(Optional.ofNullable(TenantContext.getTenantId()).orElse("default"));
        return requestRepository.save(lr);
    }

    @Transactional
    public LeaveRequest approve(@NonNull UUID requestId, UUID approverId, String note) {
        LeaveRequest lr = requestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (lr.getStatus() == LeaveStatus.APPROVED) return lr;
        if (lr.getStatus() == LeaveStatus.REJECTED) throw new IllegalStateException("Cannot approve a rejected leave");

        // Calculate working days (skip holidays if project calendars exist; since leave is general, we will prefill for each active project membership separately)
        List<LocalDate> days = enumerateDays(lr.getFromDate(), lr.getToDate());

        // Adjust balance by total hours (per project assignment we don't multiply balance by projects; balance is per employee, so hours count once per day)
        int year = lr.getFromDate().getYear();
        LeaveBalance bal = balanceRepository.findByEmployeeIdAndB2bUnitIdAndYearAndType(lr.getEmployeeId(), lr.getB2bUnitId(), year, lr.getType())
                .orElseGet(() -> {
                    LeaveBalance nb = new LeaveBalance();
                    nb.setEmployeeId(lr.getEmployeeId());
                    nb.setB2bUnitId(lr.getB2bUnitId());
                    nb.setYear(year);
                    nb.setType(lr.getType());
                    nb.setTenantId(Optional.ofNullable(TenantContext.getTenantId()).orElse("default"));
                    return nb;
                });
        BigDecimal needed = lr.getPerDayHours().multiply(new BigDecimal(days.size()));
        if (bal.getAllocated().subtract(bal.getConsumed()).compareTo(needed) < 0) {
            throw new IllegalStateException("Insufficient leave balance");
        }
        bal.setConsumed(bal.getConsumed().add(needed));
        balanceRepository.save(bal);

        // Prefill time entries: For each active project membership on that date, create a LEAVE entry against a project-scoped LEAVE WBS.
        // If multiple projects active, we split hours evenly among active memberships for that date.
        List<ProjectMember> memberships = memberRepository.findByEmployeeId(lr.getEmployeeId());
        for (LocalDate d : days) {
            List<ProjectMember> activeThatDay = memberships.stream()
                    .filter(m -> (m.getStartDate() == null || !d.isBefore(m.getStartDate()))
                            && (m.getEndDate() == null || !d.isAfter(m.getEndDate()))
                            && m.isActive())
                    .toList();
            if (activeThatDay.isEmpty()) continue;
            BigDecimal splitHours = lr.getPerDayHours().divide(new BigDecimal(activeThatDay.size()), 2, java.math.RoundingMode.HALF_UP);

            for (ProjectMember m : activeThatDay) {
                Project project = m.getProject();
                // Skip if holiday for the project's assigned calendar
                if (project.getHolidayCalendarId() != null && holidayService.isHoliday(project.getHolidayCalendarId(), d)) {
                    continue;
                }
                WBSElement leaveWbs = ensureLeaveWbs(project);
                ensureAllocationForDate(m, leaveWbs, d);
                // Avoid duplicate LEAVE entries for same date/employee
                if (timeEntryRepository.existsByEmployeeIdAndWorkDateAndEntryType(lr.getEmployeeId(), d, TimeEntryType.LEAVE)) {
                    continue;
                }
                TimeEntry te = new TimeEntry();
                te.setProject(project);
                te.setWbsElement(leaveWbs);
                te.setMember(m);
                te.setEmployeeId(lr.getEmployeeId());
                te.setWorkDate(d);
                te.setHours(splitHours);
                te.setEntryType(TimeEntryType.LEAVE);
                te.setStatus(TimeEntryStatus.DRAFT);
                te.setNotes((note != null ? note + " " : "") + "Auto-prefilled from approved leave");
                te.setTenantId(Optional.ofNullable(TenantContext.getTenantId()).orElse("default"));
                timeEntryRepository.save(te);
            }
        }

        lr.setStatus(LeaveStatus.APPROVED);
        lr.setApproverId(approverId);
        lr.setDecisionAt(java.time.Instant.now());
        lr.setNote(note);
        return requestRepository.save(lr);
    }

    @Transactional
    public LeaveRequest reject(@NonNull UUID requestId, UUID approverId, String note) {
        LeaveRequest lr = requestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        lr.setStatus(LeaveStatus.REJECTED);
        lr.setApproverId(approverId);
        lr.setDecisionAt(java.time.Instant.now());
        lr.setNote(note);
        return requestRepository.save(lr);
    }

    private List<LocalDate> enumerateDays(LocalDate from, LocalDate to) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            days.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return days;
    }

    private WBSElement ensureLeaveWbs(Project project) {
        // Find existing by code
        String tenant = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        String code = (project.getCode() != null && !project.getCode().isBlank()) ? (project.getCode() + "-LEAVE") : "LEAVE-" + project.getId();
        Optional<WBSElement> existing = wbsRepository.findByCodeAndTenantId(code, tenant);
        if (existing.isPresent()) return existing.get();
        WBSElement w = new WBSElement();
        w.setProject(project);
        w.setName("Leave");
        w.setCode(code);
        w.setCategory(WBSCategory.OTHER);
        w.setTenantId(tenant);
        return wbsRepository.save(w);
    }

    private void ensureAllocationForDate(ProjectMember member, WBSElement wbs, LocalDate date) {
        // if an active allocation already covers the date, skip; else create a one-day allocation
        List<WBSAllocation> active = allocationRepository.findByMember_IdAndActive(member.getId(), true);
        boolean covered = active.stream().anyMatch(a -> a.getWbsElement().getId().equals(wbs.getId())
                && (a.getStartDate() == null || !date.isBefore(a.getStartDate()))
                && (a.getEndDate() == null || !date.isAfter(a.getEndDate())));
        if (covered) return;
        WBSAllocation alloc = new WBSAllocation();
        alloc.setMember(member);
        alloc.setWbsElement(wbs);
        alloc.setStartDate(date);
        alloc.setEndDate(date);
        alloc.setActive(true);
        alloc.setTenantId(Optional.ofNullable(TenantContext.getTenantId()).orElse("default"));
        allocationRepository.save(alloc);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> findApprovedOverlapping(UUID employeeId, LocalDate from, LocalDate to) {
        List<LeaveRequest> overlap = requestRepository
                .findByEmployeeIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(employeeId, to, from);
        return overlap.stream().filter(lr -> lr.getStatus() == LeaveStatus.APPROVED).toList();
    }

    // Expose repository only internally via service method
    LeaveRequestRepository requestRepository() { return this.requestRepository; }
}
