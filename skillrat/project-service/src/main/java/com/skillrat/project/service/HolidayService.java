package com.skillrat.project.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.domain.HolidayCalendar;
import com.skillrat.project.domain.HolidayDay;
import com.skillrat.project.domain.Project;
import com.skillrat.project.repo.HolidayCalendarRepository;
import com.skillrat.project.repo.HolidayDayRepository;
import com.skillrat.project.repo.ProjectRepository;
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
    public HolidayCalendar createCalendar(String name, UUID b2bUnitId) {
        HolidayCalendar cal = new HolidayCalendar();
        cal.setName(name);
        cal.setB2bUnitId(b2bUnitId);
        cal.setTenantId(TenantContext.getTenantId());
        return calendarRepository.save(cal);
    }

    @Transactional
    public HolidayDay addHoliday(UUID calendarId, LocalDate date, String name, boolean optional) {
        HolidayCalendar cal = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("Calendar not found"));
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
    public Project assignCalendarToProject(UUID projectId, UUID calendarId) {
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
}
