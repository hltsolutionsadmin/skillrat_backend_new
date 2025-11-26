package com.skillrat.project.service;

import com.skillrat.project.domain.HolidayDay;
import com.skillrat.project.domain.Project;
import com.skillrat.project.domain.ProjectMember;
import com.skillrat.project.domain.TimeEntry;
import com.skillrat.project.repo.ProjectMemberRepository;
import com.skillrat.project.repo.TimeEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final ProjectMemberRepository memberRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final HolidayService holidayService;

    public SummaryService(ProjectMemberRepository memberRepository,
                          TimeEntryRepository timeEntryRepository,
                          HolidayService holidayService) {
        this.memberRepository = memberRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.holidayService = holidayService;
    }

    public WeeklySummary weeklySummary(UUID employeeId, LocalDate from) {
        LocalDate to = from.plusDays(6);
        List<ProjectMember> members = memberRepository.findByEmployeeId(employeeId);
        // Active memberships in window
        List<ProjectMember> active = members.stream().filter(m ->
                (m.getStartDate() == null || !to.isBefore(m.getStartDate())) &&
                (m.getEndDate() == null || !from.isAfter(m.getEndDate())) &&
                m.isActive()).toList();

        Map<UUID, List<TimeEntry>> entriesByDate = timeEntryRepository.findByEmployeeIdAndWorkDateBetween(employeeId, from, to)
                .stream().collect(Collectors.groupingBy(te -> toKey(te.getWorkDate())));

        List<ProjectBlock> projects = new ArrayList<>();
        for (ProjectMember m : active) {
            Project p = m.getProject();
            List<HolidayDay> holidays = Collections.emptyList();
            if (p.getHolidayCalendarId() != null) {
                holidays = holidayService.listHolidays(p.getHolidayCalendarId(), from, to);
            }
            Set<LocalDate> holidayDates = holidays.stream().map(HolidayDay::getDate).collect(Collectors.toSet());
            List<DayBlock> days = new ArrayList<>();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                List<TimeEntry> dayEntries = entriesByDate.getOrDefault(toKey(d), List.of()).stream()
                        .filter(te -> te.getProject().getId().equals(p.getId()))
                        .toList();
                days.add(new DayBlock(d, holidayDates.contains(d), dayEntries));
            }
            projects.add(new ProjectBlock(p.getId(), p.getName(), p.getCode(), days));
        }
        return new WeeklySummary(from, to, projects);
    }

    public DayContext myDay(UUID employeeId, LocalDate date) {
        List<ProjectMember> members = memberRepository.findByEmployeeId(employeeId);
        List<ProjectDay> projects = new ArrayList<>();
        List<TimeEntry> entries = timeEntryRepository.findByEmployeeIdAndWorkDate(employeeId, date);
        for (ProjectMember m : members) {
            if (!(m.isActive() && (m.getStartDate() == null || !date.isBefore(m.getStartDate())) && (m.getEndDate() == null || !date.isAfter(m.getEndDate())))) {
                continue;
            }
            Project p = m.getProject();
            boolean isHoliday = p.getHolidayCalendarId() != null && holidayService.isHoliday(p.getHolidayCalendarId(), date);
            List<TimeEntry> projectEntries = entries.stream().filter(e -> e.getProject().getId().equals(p.getId())).toList();
            projects.add(new ProjectDay(p.getId(), p.getName(), p.getCode(), isHoliday, projectEntries));
        }
        return new DayContext(date, projects);
    }

    private UUID toKey(LocalDate d) {
        // use a synthetic UUID key per date to group; not exposed externally
        return UUID.nameUUIDFromBytes(d.toString().getBytes());
    }

    public record WeeklySummary(LocalDate from, LocalDate to, List<ProjectBlock> projects) { }
    public record ProjectBlock(UUID projectId, String projectName, String projectCode, List<DayBlock> days) { }
    public record DayBlock(LocalDate date, boolean holiday, List<TimeEntry> entries) { }

    public record DayContext(LocalDate date, List<ProjectDay> projects) { }
    public record ProjectDay(UUID projectId, String projectName, String projectCode, boolean holiday, List<TimeEntry> entries) { }
}
