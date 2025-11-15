package com.skillrat.project.repo;

import com.skillrat.project.domain.HolidayDay;
import com.skillrat.project.domain.IndiaCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HolidayDayRepository extends JpaRepository<HolidayDay, UUID> {
    List<HolidayDay> findByCalendar_IdAndDateBetween(UUID calendarId, LocalDate from, LocalDate to);
    boolean existsByCalendar_IdAndDate(UUID calendarId, LocalDate date);
    long countByCalendar_Id(UUID calendarId);
    Page<HolidayDay> findByCalendar_Id(UUID calendarId, Pageable pageable);
    Page<HolidayDay> findByCalendar_IdAndDateBetween(UUID calendarId, LocalDate from, LocalDate to, Pageable pageable);
    Page<HolidayDay> findByCalendar_CityAndDateBetween(IndiaCity city, LocalDate from, LocalDate to, Pageable pageable);
}
