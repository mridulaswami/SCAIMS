package com.schoolerp.usermanagement.modules.reports.reportExpoter;

import com.schoolerp.usermanagement.modules.reports.enums.ReportFormat;
import com.schoolerp.usermanagement.modules.reports.reportGenerator.ReportData;

public interface ReportExporter {

    ReportFormat getFormat();

    byte[] export(ReportData reportData);
}
