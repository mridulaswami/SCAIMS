package com.schoolerp.usermanagement.modules.reports.reportGenerator;

import com.schoolerp.usermanagement.modules.Geometry.GeometryService;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.reports.enums.ReportType;
import com.schoolerp.usermanagement.modules.reports.requestDto.GenerateReportRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetReportGeneratorImpl implements ReportGenerator {

    private final AssetRepository assetRepository;
    private final GeometryService geometryService;

    @Override
    public ReportType getReportType() {
        log.info("Generating report for type: {}", ReportType.ASSET_REPORT);
        return ReportType.ASSET_REPORT;
    }

    @Override
    public ReportData generateReport(GenerateReportRequestDto requestDto) {

        log.info("Generating asset report | fromDate={} | toDate={}", requestDto.getFromDate(), requestDto.getToDate());

        LocalDateTime fromDateTime = requestDto.getFromDate().atStartOfDay();

        LocalDateTime toDateTime = requestDto.getToDate().plusDays(1).atStartOfDay();

        List<AssetEntity> assets = assetRepository.findByInstalledDateGreaterThanEqualAndInstalledDateLessThan(fromDateTime, toDateTime);

        List<String> headers = List.of("Asset ID", "Asset Name", "Longitude", "Latitude", "Condition", "Category", "Last Inspected", "Status", "Installed At");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        List<List<String>> rows = assets.stream().map(asset -> {

            Coordinate coordinate = asset.getGeometry().getCoordinate();

            return List.of(asset.getId().toString(), asset.getName(),

                    String.valueOf(coordinate.getX()),

                    String.valueOf(coordinate.getY()),

                    asset.getCondition() != null ? asset.getCondition().toString() : "",

                    asset.getCategoryId() != null ? asset.getCategoryId().getName() : "",

                    asset.getLastInspectionDate() != null ? asset.getLastInspectionDate().format(formatter) : "",

                    asset.getStatus() != null ? asset.getStatus().toString() : "",

                    asset.getInstalledDate() != null ? asset.getInstalledDate().format(formatter) : "");
        }).toList();

        if (assets.isEmpty()) {
            log.warn("No assets found for date range | from={} | to={}", requestDto.getFromDate(), requestDto.getToDate());
        }

        return ReportData.builder().reportName("Asset Report").headers(headers).rows(rows).build();
    }
}
