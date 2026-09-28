package com.schoolerp.usermanagement.modules.reports.service;

import com.schoolerp.usermanagement.modules.reports.entity.ReportEntity;
import com.schoolerp.usermanagement.modules.reports.enums.ReportFormat;
import com.schoolerp.usermanagement.modules.reports.enums.ReportStatus;

import com.schoolerp.usermanagement.modules.reports.factory.ReportExporterFactory;
import com.schoolerp.usermanagement.modules.reports.factory.ReportGeneratorFactory;

import com.schoolerp.usermanagement.modules.reports.reportExpoter.ReportExporter;
import com.schoolerp.usermanagement.modules.reports.reportGenerator.ReportData;
import com.schoolerp.usermanagement.modules.reports.reportGenerator.ReportGenerator;
import com.schoolerp.usermanagement.modules.reports.repository.ReportRepository;
import com.schoolerp.usermanagement.modules.reports.requestDto.GenerateReportRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReportGenerationService {

    private final ReportRepository reportRepository;
    private final ReportGeneratorFactory reportGeneratorFactory;
    private final ReportExporterFactory reportExporterFactory;

    @Async("reportTaskExecutor")
    public void generateReport(UUID reportId, GenerateReportRequestDto requestDto, UUID userId) {

        log.info("Async report generation started | reportId={} | thread={}", reportId, Thread.currentThread().getName());

        try {

            ReportEntity report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

            /*
             * 1. Find report generator
             */
            ReportGenerator generator = reportGeneratorFactory.getGenerator(requestDto.getReportType());

            /*
             * 2. Generate report data
             */
            ReportData reportData = generator.generateReport(requestDto);

            /*
             * 3. Find exporter
             */
            ReportExporter exporter = reportExporterFactory.getExporter(requestDto.getReportFormat());

            /*
             * 4. Generate actual file
             */
            byte[] file = exporter.export(reportData);

            /*
             * 5. Save/upload file
             *
             * For now we will add storage implementation here.
             */
            String fileUrl = saveReportFile(reportId, reportData.getReportName(), requestDto.getReportFormat(), file);

            /*
             * 6. Update report
             */
            report.setStatus(ReportStatus.COMPLETED);

            report.setReportCompletedAt(LocalDateTime.now());

            report.setUrl(fileUrl);

            reportRepository.save(report);

            log.info("Report generation completed | reportId={}", reportId);

        } catch (Exception e) {

            log.error("Report generation failed | reportId={}", reportId, e);

            reportRepository.findById(reportId).ifPresent(report -> {

                report.setStatus(ReportStatus.FAILED);

                report.setReportCompletedAt(LocalDateTime.now());

                report.setErrorMessage(e.getMessage());

                reportRepository.save(report);
            });
        }
    }

    private String saveReportFile(UUID reportId, String reportName, ReportFormat format, byte[] file) throws IOException {

        String uploadDirectory = "uploads/reports";

        Path reportDirectory = Paths.get(uploadDirectory, reportId.toString());

        Files.createDirectories(reportDirectory);

        String extension = switch (format) {
            case CSV -> ".csv";
            case PDF -> ".pdf";
        };

        String fileName = reportName + extension;

        Path filePath = reportDirectory.resolve(fileName);

        Files.write(filePath, file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return "/uploads/reports/" + reportId + "/" + fileName;
    }
}