package com.skillrat.project.repo;

import com.skillrat.project.domain.TimeEntry;
import com.skillrat.project.domain.TimeEntryStatus;
import com.skillrat.project.domain.TimeEntryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    List<TimeEntry> findByMember_IdAndWorkDateBetween(UUID memberId, LocalDate start, LocalDate end);
    List<TimeEntry> findByEmployeeIdAndStatus(UUID employeeId, TimeEntryStatus status);
    List<TimeEntry> findByEmployeeIdAndWorkDate(UUID employeeId, LocalDate date);
    List<TimeEntry> findByEmployeeIdAndWorkDateBetween(UUID employeeId, LocalDate start, LocalDate end);
    boolean existsByEmployeeIdAndWorkDateAndEntryType(UUID employeeId, LocalDate date, TimeEntryType entryType);
}
