package com.schoolerp.usermanagement.modules.reports.reportGenerator;

import com.schoolerp.usermanagement.modules.reports.enums.ReportType;
import com.schoolerp.usermanagement.modules.reports.requestDto.GenerateReportRequestDto;

public interface ReportGenerator {

    ReportType getReportType();

    ReportData generateReport(GenerateReportRequestDto reportRequest);
}

