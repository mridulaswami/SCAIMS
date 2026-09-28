package com.schoolerp.usermanagement.modules.reports.reportExpoter;

import com.schoolerp.usermanagement.modules.reports.enums.ReportFormat;
import com.schoolerp.usermanagement.modules.reports.reportGenerator.ReportData;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class CsvReportExporter implements ReportExporter {

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.CSV;
    }

    @Override
    public byte[] export(ReportData reportData) {

        StringBuilder csv = new StringBuilder();

        // Headers
        csv.append(String.join(",", reportData.getHeaders()));

        csv.append("\n");

        // Rows
        for (var row : reportData.getRows()) {

            csv.append(row.stream().map(this::escapeCsvValue).reduce((a, b) -> a + "," + b).orElse(""));

            csv.append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsvValue(Object value) {

        if (value == null) {
            return "";
        }

        String text = value.toString();

        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {

            text = text.replace("\"", "\"\"");
            return "\"" + text + "\"";
        }

        return text;
    }
}
