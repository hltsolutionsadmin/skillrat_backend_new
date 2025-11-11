package com.skillrat.project.repo;

import com.skillrat.project.domain.HolidayDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HolidayDayRepository extends JpaRepository<HolidayDay, UUID> {
    List<HolidayDay> findByCalendar_IdAndDateBetween(UUID calendarId, LocalDate from, LocalDate to);
    boolean existsByCalendar_IdAndDate(UUID calendarId, LocalDate date);
}
