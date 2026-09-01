package com.schoolerp.usermanagement.modules.inspection.controller;

import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
import com.schoolerp.usermanagement.modules.inspection.requestDto.CreateInspectionRequest;
import com.schoolerp.usermanagement.modules.inspection.responseDto.InspectionResponse;
import com.schoolerp.usermanagement.modules.inspection.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {
    private final InspectionService inspectionService;

    @PostMapping
    public ResponseEntity<InspectionResponse> createInspection(
            @RequestBody CreateInspectionRequest request){
        InspectionEntity inspection = inspectionService.createInspection(
                request.getAssetId(),
                request.getInspectorUserId(),
                request.getNotes(),
                request.getLatitude(),
                request.getLongitude()
        );

        InspectionResponse response = new InspectionResponse();

        response.setId(inspection.getId());
        response.setAssetId(inspection.getAssetId().getId());
        response.setInspectorUserId(inspection.getInspectorUserId().getId());
        response.setNotes(inspection.getNotes());
        response.setInspectedAt(inspection.getInspectedAt());

        return ResponseEntity.ok(response);

    }

    @GetMapping
    public ResponseEntity<List<InspectionResponse>> getAllInspections() {

        List<InspectionEntity> inspections =
                inspectionService.getAllInspections();

        List<InspectionResponse> response = inspections.stream()
                .map(inspection -> {
                    InspectionResponse dto = new InspectionResponse();

                    dto.setId(inspection.getId());
                    dto.setAssetId(inspection.getAssetId().getId());
                    dto.setInspectorUserId(inspection.getInspectorUserId().getId());
                    dto.setNotes(inspection.getNotes());
                    dto.setInspectedAt(inspection.getInspectedAt());

                    if (inspection.getGeoTag() != null) {
                        dto.setLongitude(
                                inspection.getGeoTag().getX()
                        );

                        dto.setLatitude(
                                inspection.getGeoTag().getY()
                        );
                    }

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InspectionResponse> getInspectionById(
            @PathVariable UUID id) {

        InspectionEntity inspection =
                inspectionService.getInspectionById(id);

        InspectionResponse response = new InspectionResponse();

        response.setId(inspection.getId());
        response.setAssetId(inspection.getAssetId().getId());
        response.setInspectorUserId(
                inspection.getInspectorUserId().getId()
        );
        response.setNotes(inspection.getNotes());
        response.setInspectedAt(inspection.getInspectedAt());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<InspectionResponse>> getInspectionsByAssetId(
            @PathVariable UUID assetId) {

        List<InspectionEntity> inspections =
                inspectionService.getInspectionsByAssetId(assetId);

        List<InspectionResponse> responses = inspections.stream()
                .map(inspection -> {

                    InspectionResponse response = new InspectionResponse();

                    response.setId(inspection.getId());
                    response.setAssetId(inspection.getAssetId().getId());
                    response.setInspectorUserId(inspection.getInspectorUserId().getId());
                    response.setNotes(inspection.getNotes());
                    response.setInspectedAt(inspection.getInspectedAt());

                    return response;
                })
                .toList();

        return ResponseEntity.ok(responses);
    }

}
