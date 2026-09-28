package com.schoolerp.usermanagement.modules.reports.reportGenerator;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportData {
    private String reportName;
    private List<String> headers;
    private List<List<String>> rows;
}