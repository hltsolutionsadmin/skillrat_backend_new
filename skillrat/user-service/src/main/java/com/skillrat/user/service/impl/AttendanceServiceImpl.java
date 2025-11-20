package com.skillrat.user.service.impl;

import com.skillrat.user.repo.EmployeeAttendanceDao;
import com.skillrat.user.dto.AttendanceDTO;
import com.skillrat.user.domain.EmployeeAttendance;
import com.skillrat.user.service.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final EmployeeAttendanceDao attendanceDao;

    public AttendanceServiceImpl(EmployeeAttendanceDao attendanceDao) {
        this.attendanceDao = attendanceDao;
    }

    @Override
    public AttendanceDTO mark(AttendanceDTO req) {

        int month = req.getDate().getMonthValue();
        int year = req.getDate().getYear();

        // Check if entry exists
        if (attendanceDao.existsByEmployeeIdAndYearAndMonthAndDate(req.getEmployeeId(), year, month,req.getDate())) {

            EmployeeAttendance existing = attendanceDao
                    .findByEmployeeIdAndDateBetween(req.getEmployeeId(), req.getDate(), req.getDate())
                    .stream()
                    .findFirst()
                    .orElseThrow();

            existing.setStatus(req.getStatus());
            existing.setMonth(month);
            existing.setYear(year);

            return toResp(attendanceDao.save(existing));
        }

        // Create new entry
        EmployeeAttendance ea = EmployeeAttendance.builder()
                .employeeId(req.getEmployeeId())
                .date(req.getDate())
                .status(req.getStatus())
                .month(month)
                .year(year)
                .build();

        return toResp(attendanceDao.save(ea));
    }


    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getMonth(UUID employeeId, int month, int year) {
        List<AttendanceDTO> attendancelist = new ArrayList<>();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        List<EmployeeAttendance> list = attendanceDao.findByEmployeeIdAndDateBetween(employeeId, from, to);
        attendancelist=list.stream().map(this::toResp).collect(Collectors.toList());
        return attendancelist;
    }

    private AttendanceDTO toResp(EmployeeAttendance ea) {
        return AttendanceDTO.builder()
                .id(ea.getId())
                .employeeId(ea.getEmployeeId())
                .date(ea.getDate())
                .status(ea.getStatus())
                .build();
    }
}
