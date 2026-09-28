package com.schoolerp.usermanagement.modules.reports.factory;

import com.schoolerp.usermanagement.modules.reports.enums.ReportType;
import com.schoolerp.usermanagement.modules.reports.reportGenerator.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportGeneratorFactory {

    private final Map<ReportType, ReportGenerator> generatorMap;

    public ReportGeneratorFactory(List<ReportGenerator> generators) {

        this.generatorMap = new EnumMap<>(ReportType.class);

        for (ReportGenerator generator : generators) {

            ReportGenerator existing = generatorMap.put(generator.getReportType(), generator);

            if (existing != null) {
                throw new IllegalStateException("Multiple generators found for report type: " + generator.getReportType());
            }
        }
    }

    public ReportGenerator getGenerator(ReportType reportType) {

        ReportGenerator generator = generatorMap.get(reportType);

        if (generator == null) {
            throw new IllegalArgumentException("No report generator found for report type: " + reportType);
        }

        return generator;
    }
}
