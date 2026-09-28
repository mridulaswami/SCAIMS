package com.schoolerp.usermanagement.modules.reports.factory;

import com.schoolerp.usermanagement.modules.reports.enums.ReportFormat;
import com.schoolerp.usermanagement.modules.reports.reportExpoter.ReportExporter;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportExporterFactory {

    private final Map<ReportFormat, ReportExporter> exporterMap;

    public ReportExporterFactory(List<ReportExporter> exporters) {

        this.exporterMap = new EnumMap<>(ReportFormat.class);

        for (ReportExporter exporter : exporters) {

            ReportExporter existing = exporterMap.put(exporter.getFormat(), exporter);

            if (existing != null) {
                throw new IllegalStateException("Multiple exporters found for format: " + exporter.getFormat());
            }
        }
    }

    public ReportExporter getExporter(ReportFormat format) {

        ReportExporter exporter = exporterMap.get(format);

        if (exporter == null) {
            throw new IllegalArgumentException("No exporter found for format: " + format);
        }

        return exporter;
    }
}
