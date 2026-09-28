package com.schoolerp.usermanagement.modules.reports.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.common.response.PaginationResponse;
import com.schoolerp.usermanagement.modules.reports.requestDto.GenerateReportRequestDto;
import com.schoolerp.usermanagement.modules.reports.responseDto.GenerateReportResponseDto;
import com.schoolerp.usermanagement.modules.reports.responseDto.GetReportResponseDto;
import com.schoolerp.usermanagement.modules.reports.service.ReportService;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GenerateReportResponseDto>> generateReport(@RequestBody GenerateReportRequestDto requestDto, HttpServletRequest request) {

        UUID currentUserId = extractUserId(request);

        log.info("POST /api/v1/reports/generate | userId={} | reportType={} | format={}", currentUserId, requestDto.getReportType(), requestDto.getReportFormat());

        GenerateReportResponseDto response = reportService.generateReport(requestDto, currentUserId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.of(true, "Report generation started successfully", response));
    }


    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<GetReportResponseDto>> getReportById(@PathVariable UUID reportId, HttpServletRequest request) {

        UUID currentUserId = extractUserId(request);

        log.info("GET /api/v1/reports/{} | userId={}", reportId, currentUserId);

        GetReportResponseDto response = reportService.getReportById(reportId);

        return ResponseEntity.ok(ApiResponse.of(true, "Report fetched successfully", response));
    }


    @GetMapping("/my-reports")
    public ResponseEntity<PaginationResponse<List<GetReportResponseDto>>> getMyReports(HttpServletRequest request, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        UUID currentUserId = extractUserId(request);

        log.info("GET /api/v1/reports/my-reports | userId={} | page={} | size={}", currentUserId, page, size);

        PaginationResponse<List<GetReportResponseDto>> response = reportService.getReportByUserId(currentUserId, page, size);

        return ResponseEntity.ok(response);
    }

    private UUID extractUserId(HttpServletRequest request) {

        String token = jwtTokenProvider.extractAccestoken(request);

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Unauthorized: Token is missing");
        }

        String userIdStr = jwtTokenProvider.getUserIdFromJWT(token);

        if (userIdStr == null || userIdStr.isBlank()) {
            throw new RuntimeException("Unauthorized: Invalid token claims");
        }

        return UUID.fromString(userIdStr);
    }
}