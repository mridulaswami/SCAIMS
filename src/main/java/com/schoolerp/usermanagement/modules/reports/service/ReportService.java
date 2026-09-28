package com.schoolerp.usermanagement.modules.reports.service;

import com.schoolerp.usermanagement.common.response.PaginationResponse;
import com.schoolerp.usermanagement.modules.reports.requestDto.GenerateReportRequestDto;
import com.schoolerp.usermanagement.modules.reports.responseDto.GenerateReportResponseDto;
import com.schoolerp.usermanagement.modules.reports.responseDto.GetReportResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface ReportService {

    GenerateReportResponseDto generateReport(GenerateReportRequestDto requestDto, UUID userId);

    GetReportResponseDto getReportById(UUID reportId);

    PaginationResponse<List<GetReportResponseDto>> getReportByUserId(UUID userId, int page, int size);
}
