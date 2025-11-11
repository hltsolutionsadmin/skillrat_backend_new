package com.skillrat.project.repo;

import com.skillrat.project.domain.HolidayCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, UUID> {
    Optional<HolidayCalendar> findByNameAndTenantId(String name, String tenantId);
}
