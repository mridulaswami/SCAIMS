package com.schoolerp.usermanagement.modules.reports.responseDto;

import com.schoolerp.usermanagement.modules.reports.enums.ReportFormat;
import com.schoolerp.usermanagement.modules.reports.enums.ReportStatus;
import com.schoolerp.usermanagement.modules.reports.enums.ReportType;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetReportResponseDto {

    private UUID reportId;
    private ReportType reportType;
    private ReportStatus reportStatus;
    private String reportName;
    private ReportFormat reportFormat;
    private String url;
    private String createdBy;
    private LocalDateTime reportCreatedDate;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String errorMessage;
}
