package com.skillrat.user.service;

import com.skillrat.user.dto.LeaveDTO;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public interface LeaveService {
    LeaveDTO apply(LeaveDTO req);
    LeaveDTO approve(UUID id);
    LeaveDTO reject(UUID id);
    List<LeaveDTO> listByEmployee(UUID employeeId);
    List<LeaveDTO> listApprovedOverlapping(UUID employeeId, LocalDate from, LocalDate to);
}
