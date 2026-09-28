package com.schoolerp.usermanagement.modules.reports.responseDto;

import com.schoolerp.usermanagement.modules.reports.enums.ReportFormat;
import com.schoolerp.usermanagement.modules.reports.enums.ReportStatus;
import com.schoolerp.usermanagement.modules.reports.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReportResponseDto {

    private UUID reportId;
    private ReportStatus reportStatus;
    private ReportType reportType;
    private ReportFormat reportFormat;
}
