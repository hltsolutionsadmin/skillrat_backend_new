package com.skillrat.project.web;

import com.skillrat.project.domain.HolidayCalendar;
import com.skillrat.project.domain.HolidayDay;
import com.skillrat.project.service.HolidayService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/holidays")
@Validated
public class HolidayController {

    private final HolidayService service;

    public HolidayController(HolidayService service) { this.service = service; }

    @PostMapping("/calendars")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<HolidayCalendar> createCalendar(@RequestBody @Valid CreateCalReq req) {
        return ResponseEntity.ok(service.createCalendar(req.name, req.b2bUnitId));
    }

    @PostMapping("/calendars/{calendarId}/days")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<HolidayDay> addHoliday(@PathVariable("calendarId") UUID calendarId,
                                                 @RequestBody @Valid AddHolidayReq req) {
        return ResponseEntity.ok(service.addHoliday(calendarId, req.date, req.name, req.optional));
    }

    @PutMapping("/projects/{projectId}/calendar/{calendarId}")
    @PreAuthorize("hasAnyRole('ADMIN','PMO','PROJECT_MANAGER')")
    public ResponseEntity<?> assignToProject(@PathVariable UUID projectId, @PathVariable UUID calendarId) {
        return ResponseEntity.ok(service.assignCalendarToProject(projectId, calendarId));
    }

    @GetMapping("/calendars/{calendarId}/days")
    @PreAuthorize("isAuthenticated()")
    public List<HolidayDay> list(@PathVariable UUID calendarId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.listHolidays(calendarId, from, to);
    }

    public static class CreateCalReq {
        @NotBlank public String name;
        public UUID b2bUnitId;
    }

    public static class AddHolidayReq {
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) public LocalDate date;
        @NotBlank public String name;
        public boolean optional;
    }
}
