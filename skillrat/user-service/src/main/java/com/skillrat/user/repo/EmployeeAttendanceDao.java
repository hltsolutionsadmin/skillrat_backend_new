package com.skillrat.user.repo;

import com.skillrat.user.domain.EmployeeAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmployeeAttendanceDao extends JpaRepository<EmployeeAttendance, UUID> {
    List<EmployeeAttendance> findByEmployeeIdAndDateBetween(UUID employeeId, LocalDate from, LocalDate to);
    boolean existsByEmployeeIdAndDate(UUID employeeId, LocalDate date);

    boolean existsByEmployeeIdAndYearAndMonthAndDate(UUID employeeId, int year, int month, LocalDate date);

}
