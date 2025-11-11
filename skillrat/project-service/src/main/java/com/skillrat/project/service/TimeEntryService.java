package com.skillrat.project.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.domain.*;
import com.skillrat.project.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryApprovalRepository approvalRepository;
    private final ProjectMemberRepository memberRepository;
    private final WBSElementRepository wbsRepository;
    private final ProjectRepository projectRepository;
    private final WBSAllocationRepository allocationRepository;

    public TimeEntryService(TimeEntryRepository timeEntryRepository,
                            TimeEntryApprovalRepository approvalRepository,
                            ProjectMemberRepository memberRepository,
                            WBSElementRepository wbsRepository,
                            ProjectRepository projectRepository,
                            WBSAllocationRepository allocationRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.approvalRepository = approvalRepository;
        this.memberRepository = memberRepository;
        this.wbsRepository = wbsRepository;
        this.projectRepository = projectRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional
    public TimeEntry createDraft(UUID projectId, UUID wbsId, UUID memberId, UUID employeeId,
                                 LocalDate workDate, BigDecimal hours, String notes) {
        ProjectMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Project member not found"));
        WBSElement wbs = wbsRepository.findById(wbsId)
                .orElseThrow(() -> new IllegalArgumentException("WBS not found"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!member.getProject().getId().equals(project.getId())) {
            throw new IllegalStateException("Member does not belong to project");
        }
        if (!wbs.getProject().getId().equals(project.getId())) {
            throw new IllegalStateException("WBS does not belong to project");
        }
        if (!member.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Employee does not match member");
        }
        ensureActiveAllocationOnDate(memberId, wbsId, workDate);

        TimeEntry te = new TimeEntry();
        te.setProject(project);
        te.setWbsElement(wbs);
        te.setMember(member);
        te.setEmployeeId(employeeId);
        te.setWorkDate(workDate);
        te.setHours(hours);
        te.setNotes(notes);
        te.setStatus(TimeEntryStatus.DRAFT);
        te.setTenantId(Optional.ofNullable(TenantContext.getTenantId()).orElse("default"));
        return timeEntryRepository.save(te);
    }

    @Transactional
    public TimeEntry submit(UUID timeEntryId) {
        TimeEntry te = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found"));
        ensureActiveAllocationOnDate(te.getMember().getId(), te.getWbsElement().getId(), te.getWorkDate());
        te.setStatus(TimeEntryStatus.SUBMITTED);
        return timeEntryRepository.save(te);
    }

    @Transactional
    public TimeEntry approve(UUID timeEntryId, UUID approverId, String note) {
        TimeEntry te = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found"));
        if (te.getStatus() == TimeEntryStatus.REJECTED) {
            throw new IllegalStateException("Cannot approve a rejected time entry");
        }
        // Record approval
        TimeEntryApproval ap = new TimeEntryApproval();
        ap.setTimeEntry(te);
        ap.setApproverId(approverId);
        ap.setApproverNote(note);
        ap.setTenantId(Optional.ofNullable(TenantContext.getTenantId()).orElse("default"));
        approvalRepository.save(ap);
        // Any one manager can approve -> mark APPROVED immediately
        te.setStatus(TimeEntryStatus.APPROVED);
        return timeEntryRepository.save(te);
    }

    @Transactional
    public TimeEntry reject(UUID timeEntryId, UUID approverId, String note) {
        TimeEntry te = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found"));
        te.setStatus(TimeEntryStatus.REJECTED);
        // Optionally record a rejection as an approval record with note
        TimeEntryApproval ap = new TimeEntryApproval();
        ap.setTimeEntry(te);
        ap.setApproverId(approverId);
        ap.setApproverNote(note != null ? ("REJECT: " + note) : "REJECT");
        ap.setTenantId(Optional.ofNullable(TenantContext.getTenantId()).orElse("default"));
        approvalRepository.save(ap);
        return timeEntryRepository.save(te);
    }

    private void ensureActiveAllocationOnDate(UUID memberId, UUID wbsId, LocalDate date) {
        // Fast path: fetch active allocations for member and wbs and check date window
        List<WBSAllocation> active = allocationRepository.findByMember_IdAndActive(memberId, true);
        boolean ok = active.stream().anyMatch(a ->
                a.getWbsElement().getId().equals(wbsId)
                        && (a.isActive())
                        && (a.getStartDate() == null || !date.isBefore(a.getStartDate()))
                        && (a.getEndDate() == null || !date.isAfter(a.getEndDate()))
        );
        if (!ok) {
            throw new IllegalStateException("No active WBS allocation for member on given date");
        }
    }
}
