package com.skillrat.project.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.domain.HolidayCalendar;
import com.skillrat.project.domain.HolidayDay;
import com.skillrat.project.domain.IndiaCity;
import com.skillrat.project.domain.Project;
import com.skillrat.project.repo.HolidayCalendarRepository;
import com.skillrat.project.repo.HolidayDayRepository;
import com.skillrat.project.repo.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class HolidayService {

    private final HolidayCalendarRepository calendarRepository;
    private final HolidayDayRepository dayRepository;
    private final ProjectRepository projectRepository;

    public HolidayService(HolidayCalendarRepository calendarRepository,
                          HolidayDayRepository dayRepository,
                          ProjectRepository projectRepository) {
        this.calendarRepository = calendarRepository;
        this.dayRepository = dayRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public HolidayCalendar createCalendar(String name, String code, IndiaCity city, UUID b2bUnitId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        HolidayCalendar cal = new HolidayCalendar();
        cal.setName(name);
        if (code != null && !code.isBlank()) {
            String tenant = TenantContext.getTenantId();
            calendarRepository.findByCodeAndTenantId(code, tenant).ifPresent(c -> {
                throw new IllegalStateException("Calendar code already exists for tenant");
            });
            cal.setCode(code);
        }
        cal.setCity(city);
        cal.setB2bUnitId(b2bUnitId);
        cal.setTenantId(TenantContext.getTenantId());
        return calendarRepository.save(cal);
    }

    @Transactional
    public HolidayDay addHoliday(@NonNull UUID calendarId, LocalDate date, String name, boolean optional) {
        HolidayCalendar cal = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("Calendar not found"));
        if (date == null) throw new IllegalArgumentException("date is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("holiday name is required");
        if (dayRepository.existsByCalendar_IdAndDate(calendarId, date)) {
            throw new IllegalStateException("Holiday already exists for date");
        }
        HolidayDay day = new HolidayDay();
        day.setCalendar(cal);
        day.setDate(date);
        day.setName(name);
        day.setOptionalHoliday(optional);
        day.setTenantId(TenantContext.getTenantId());
        return dayRepository.save(day);
    }

    @Transactional
    public Project assignCalendarToProject(@NonNull UUID projectId, @NonNull UUID calendarId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        if (!calendarRepository.existsById(calendarId)) {
            throw new IllegalArgumentException("Calendar not found");
        }
        project.setHolidayCalendarId(calendarId);
        return projectRepository.save(project);
    }

    public boolean isHoliday(UUID calendarId, LocalDate date) {
        return dayRepository.existsByCalendar_IdAndDate(calendarId, date);
    }

    public List<HolidayDay> listHolidays(UUID calendarId, LocalDate from, LocalDate to) {
        return dayRepository.findByCalendar_IdAndDateBetween(calendarId, from, to);
    }

    public Page<HolidayCalendarRepository.HolidayCalendarSummary> searchCalendars(String q, IndiaCity city, Pageable pageable) {
        String tenant = TenantContext.getTenantId();
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return calendarRepository.searchCalendars(tenant, query, city, pageable);
    }

    public HolidayCalendar getCalendar(@NonNull UUID id) {
        return calendarRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Calendar not found"));
    }

    @SuppressWarnings("null")
	@Transactional
    public HolidayCalendar updateCalendar(@NonNull UUID id, String name, String code, IndiaCity city) {
        HolidayCalendar cal = calendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Calendar not found"));
        if (name != null && !name.isBlank()) cal.setName(name.trim());
        if (code != null && !code.isBlank()) {
            String tenant = TenantContext.getTenantId();
            calendarRepository.findByCodeAndTenantId(code, tenant)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> { throw new IllegalStateException("Calendar code already exists for tenant"); });
            cal.setCode(code.trim());
        } else if (code != null) {
            cal.setCode(null);
        }
        if (city != null) cal.setCity(city);
        return calendarRepository.save(cal);
    }

    @Transactional
    public void deleteCalendar(@NonNull UUID id) {
        // It is allowed if not referenced by any project
        boolean inUse = projectRepository.findAll().stream().anyMatch(p -> id.equals(p.getHolidayCalendarId()));
        if (inUse) throw new IllegalStateException("Calendar is assigned to a project and cannot be deleted");
        calendarRepository.deleteById(id);
    }

    public Page<HolidayDay> listHolidays(UUID calendarId, LocalDate from, LocalDate to, Pageable pageable) {
        if (from != null && to != null) {
            return dayRepository.findByCalendar_IdAndDateBetween(calendarId, from, to, pageable);
        }
        return dayRepository.findByCalendar_Id(calendarId, pageable);
    }

    public Page<HolidayDay> listHolidaysByCityAndYear(IndiaCity city, int year, Pageable pageable) {
        if (city == null) throw new IllegalArgumentException("city is required");
        if (year < 1900 || year > 3000) throw new IllegalArgumentException("year is invalid");
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        return dayRepository.findByCalendar_CityAndDateBetween(city, from, to, pageable);
    }
}

