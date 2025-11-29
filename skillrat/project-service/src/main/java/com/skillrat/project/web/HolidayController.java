package com.skillrat.project.web;

import com.skillrat.project.domain.HolidayCalendar;
import com.skillrat.project.domain.HolidayDay;
import com.skillrat.project.domain.IndiaCity;
import com.skillrat.project.service.HolidayService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/holidays")
@Validated
public class HolidayController {

    private final HolidayService service;

    public HolidayController(HolidayService service) { this.service = service; }

    // List calendars with pagination, search and city filter
    @GetMapping("/calendars")
    @PreAuthorize("isAuthenticated()")
    public Page<CalendarSummaryDTO> searchCalendars(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "city", required = false) IndiaCity city,
            Pageable pageable
    ) {
        return service.searchCalendars(q, city, pageable).map(CalendarSummaryDTO::from);
    }

    @PostMapping("/calendars")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HolidayCalendar> createCalendar(@RequestBody @Valid CreateCalReq req) {
        return ResponseEntity.ok(service.createCalendar(req.name, req.code, req.city, req.b2bUnitId));
    }

    @GetMapping("/calendars/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HolidayCalendar> getCalendar(@PathVariable("id") @NonNull UUID id) {
        return ResponseEntity.ok(service.getCalendar(id));
    }

    @PutMapping("/calendars/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HolidayCalendar> updateCalendar(@PathVariable("id") @NonNull UUID id,
                                                          @RequestBody @Valid UpdateCalReq req) {
        return ResponseEntity.ok(service.updateCalendar(id, req.name, req.code, req.city));
    }

    @DeleteMapping("/calendars/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCalendar(@PathVariable("id") @NonNull UUID id) {
        service.deleteCalendar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/calendars/{calendarId}/days")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HolidayDay> addHoliday(@PathVariable("calendarId") @NonNull UUID calendarId,
                                                 @RequestBody @Valid AddHolidayReq req) {
        return ResponseEntity.ok(service.addHoliday(calendarId, req.date, req.name, req.optional));
    }

    @PutMapping("/projects/{projectId}/calendar/{calendarId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> assignToProject(@PathVariable @NonNull UUID projectId, @PathVariable @NonNull UUID calendarId) {
        return ResponseEntity.ok(service.assignCalendarToProject(projectId, calendarId));
    }

    @GetMapping("/calendars/{calendarId}/days")
    @PreAuthorize("isAuthenticated()")
    public Page<HolidayDay> list(@PathVariable UUID calendarId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                 Pageable pageable) {
        return service.listHolidays(calendarId, from, to, pageable);
    }

    // List holidays by city and year with pagination
    @GetMapping("/days")
    @PreAuthorize("isAuthenticated()")
    public Page<HolidayDay> listByCityAndYear(@RequestParam("city") IndiaCity city,
                                              @RequestParam("year") int year,
                                              Pageable pageable) {
        return service.listHolidaysByCityAndYear(city, year, pageable);
    }

    public static class CreateCalReq {
        @NotBlank public String name;
        public String code;
        public IndiaCity city;
        public UUID b2bUnitId;
    }

    public static class AddHolidayReq {
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) public LocalDate date;
        @NotBlank public String name;
        public boolean optional;
    }

    public static class UpdateCalReq {
        public String name;
        public String code;
        public IndiaCity city;
    }

    public static class CalendarSummaryDTO {
        public UUID id;
        public String code;
        public String name;
        public IndiaCity city;
        public Long holidayCount;
        public String sampleProjectName;

        public static CalendarSummaryDTO from(com.skillrat.project.repo.HolidayCalendarRepository.HolidayCalendarSummary p) {
            CalendarSummaryDTO dto = new CalendarSummaryDTO();
            dto.id = p.getId();
            dto.code = p.getCode();
            dto.name = p.getName();
            dto.city = p.getCity();
            dto.holidayCount = p.getHolidayCount();
            dto.sampleProjectName = p.getSampleProjectName();
            return dto;
        }
    }
}

