package com.schoolerp.usermanagement.modules.reports.service.impl;

import com.schoolerp.usermanagement.common.response.PaginationResponse;
import com.schoolerp.usermanagement.modules.auth.entity.UserRoleEntity;
import com.schoolerp.usermanagement.modules.auth.repository.UserRoleRepository;
import com.schoolerp.usermanagement.modules.reports.entity.ReportEntity;
import com.schoolerp.usermanagement.modules.reports.enums.ReportStatus;
import com.schoolerp.usermanagement.modules.reports.repository.ReportRepository;
import com.schoolerp.usermanagement.modules.reports.requestDto.GenerateReportRequestDto;
import com.schoolerp.usermanagement.modules.reports.responseDto.GenerateReportResponseDto;
import com.schoolerp.usermanagement.modules.reports.responseDto.GetReportResponseDto;
import com.schoolerp.usermanagement.modules.reports.service.ReportService;
import com.schoolerp.usermanagement.modules.reports.service.AsyncReportGenerationService;
import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final UserRoleRepository userRoleRepository;

    private final ReportRepository reportRepository;
    private final AsyncReportGenerationService asyncReportGenerationService;

    @Override
    @Transactional
    public GenerateReportResponseDto generateReport(GenerateReportRequestDto requestDto, UUID userId) {

        log.info("Report generation requested | type={} | format={} | userId={}", requestDto.getReportType(), requestDto.getReportFormat(), userId);

        ReportEntity report = new ReportEntity();

        report.setReportType(requestDto.getReportType());
        report.setStatus(ReportStatus.IN_PROGRESS);
        report.setCreatedBy(UserEntity.builder().id(userId).build());
        report.setReportFormat(requestDto.getReportFormat());
        report.setFromDate(requestDto.getFromDate());
        report.setToDate(requestDto.getToDate());

        report.setReportName(requestDto.getReportType().name() + "_" + requestDto.getReportFormat().name());

        ReportEntity savedReport = reportRepository.saveAndFlush(report);

        log.info("Report created | reportId={} | status={}", savedReport.getId(), savedReport.getStatus());

        asyncReportGenerationService.generateReport(savedReport.getId(), requestDto, userId);

        return GenerateReportResponseDto.builder().reportId(savedReport.getId()).reportType(savedReport.getReportType()).reportFormat(requestDto.getReportFormat()).reportStatus(savedReport.getStatus()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetReportResponseDto getReportById(UUID reportId) {

        if (reportId == null) {
            throw new IllegalArgumentException("reportId is null");
        }
        ReportEntity report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));

        return GetReportResponseDto.builder().reportId(report.getId()).reportType(report.getReportType()).reportStatus(report.getStatus()).reportName(report.getReportName()).url(report.getUrl()).createdBy(report.getCreatedBy().getName()).reportCreatedDate(report.getReportInitiatedAt()).fromDate(report.getFromDate()).toDate(report.getToDate()).errorMessage(report.getErrorMessage()).build();
    }


    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<GetReportResponseDto>> getReportByUserId(UUID userId, int page, int size) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ReportEntity> reportPage = reportRepository.findByCreatedByIdOrderByCreatedAtDesc(userId, pageable);

        List<GetReportResponseDto> reportDtos = reportPage.getContent().stream().map(report -> GetReportResponseDto.builder().reportId(report.getId()).reportType(report.getReportType()).reportStatus(report.getStatus()).reportName(report.getReportName()).url(report.getUrl()).createdBy(report.getCreatedBy() != null ? report.getCreatedBy().getName() : null).reportCreatedDate(report.getReportInitiatedAt()).fromDate(report.getFromDate()).toDate(report.getToDate()).errorMessage(report.getErrorMessage()).build()).toList();

        PaginationResponse<List<GetReportResponseDto>> response = new PaginationResponse<>(reportDtos, reportPage.getTotalElements(), reportPage.getNumber(), reportPage.getSize());

        response.setSuccess(true);
        response.setMessage("User reports fetched successfully");

        return response;
    }

}