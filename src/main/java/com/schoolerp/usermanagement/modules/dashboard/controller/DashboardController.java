package com.schoolerp.usermanagement.modules.dashboard.controller;

import com.schoolerp.usermanagement.modules.dashboard.responseDto.DashboardResponse;
import com.schoolerp.usermanagement.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }
}