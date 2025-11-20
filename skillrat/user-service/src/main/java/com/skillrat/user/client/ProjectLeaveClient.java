package com.skillrat.user.client;

import com.skillrat.user.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "project-service", configuration = FeignClientConfig.class)
public interface ProjectLeaveClient {

    @GetMapping("/api/leave/approved/{employeeId}/{month}/{year}")
    List<ApprovedLeaveDTO> getApprovedLeaves(@PathVariable("employeeId") java.util.UUID employeeId,
                                             @PathVariable("month") int month,
                                             @PathVariable("year") int year);

    class ApprovedLeaveDTO {
        public String leaveType;
        public LocalDate startDate;
        public LocalDate endDate;
        public String status;
    }
}
