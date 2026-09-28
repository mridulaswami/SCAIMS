package com.schoolerp.usermanagement.modules.reports.requestDto;

import com.schoolerp.usermanagement.modules.reports.enums.ReportFormat;
import com.schoolerp.usermanagement.modules.reports.enums.ReportType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReportRequestDto {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    @Enumerated(EnumType.STRING)
    private ReportType reportType;
    @Enumerated(EnumType.STRING)
    private ReportFormat reportFormat;
}
