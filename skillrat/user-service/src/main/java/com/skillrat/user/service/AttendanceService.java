package com.skillrat.user.service;

import com.skillrat.user.dto.AttendanceDTO;

import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    AttendanceDTO mark(AttendanceDTO req);
    List<AttendanceDTO> getMonth(UUID employeeId, int month, int year);
}
