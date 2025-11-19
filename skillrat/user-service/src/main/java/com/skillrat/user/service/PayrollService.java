package com.skillrat.user.service;

import com.skillrat.user.dto.PayslipDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollService {
    PayslipDTO generate(PayslipDTO req);
    Optional<PayslipDTO> get(UUID employeeId, int month, int year);
    List<PayslipDTO> listByEmployee(UUID employeeId);
}
