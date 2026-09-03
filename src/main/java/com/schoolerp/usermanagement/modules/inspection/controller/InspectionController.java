//package com.schoolerp.usermanagement.modules.inspection.controller;
//
//import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
//import com.schoolerp.usermanagement.modules.inspection.requestDto.CreateInspectionRequest;
//import com.schoolerp.usermanagement.modules.inspection.responseDto.InspectionResponse;
//import com.schoolerp.usermanagement.modules.inspection.service.InspectionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/inspections")
//@RequiredArgsConstructor
//public class InspectionController {
//    private final InspectionService inspectionService;
//
//    @PostMapping
//    public ResponseEntity<InspectionResponse> createInspection(
//            @RequestBody CreateInspectionRequest request){
//        InspectionEntity inspection = inspectionService.createInspection(
//                request.getAssetId(),
//                request.getInspectorUserId(),
//                request.getNotes(),
//                request.getLatitude(),
//                request.getLongitude()
//        );
//
//        InspectionResponse response = new InspectionResponse();
//
//        response.setId(inspection.getId());
//        response.setAssetId(inspection.getAssetId().getId());
//        response.setInspectorUserId(inspection.getInspectorUserId().getId());
//        response.setNotes(inspection.getNotes());
//        response.setInspectedAt(inspection.getInspectedAt());
//
//        return ResponseEntity.ok(response);
//
//    }
//
//
//    @GetMapping
//    public ResponseEntity<List<InspectionResponse>> getAllInspections() {
//
//        List<InspectionEntity> inspections =
//                inspectionService.getAllInspections();
//
//        List<InspectionResponse> response = inspections.stream()
//                .map(inspection -> {
//                    InspectionResponse dto = new InspectionResponse();
//
//                    dto.setId(inspection.getId());
//                    dto.setAssetId(inspection.getAssetId().getId());
//                    dto.setInspectorUserId(inspection.getInspectorUserId().getId());
//                    dto.setNotes(inspection.getNotes());
//                    dto.setInspectedAt(inspection.getInspectedAt());
//
//                    if (inspection.getGeoTag() != null) {
//                        dto.setLongitude(
//                                inspection.getGeoTag().getX()
//                        );
//
//                        dto.setLatitude(
//                                inspection.getGeoTag().getY()
//                        );
//                    }
//
//                    return dto;
//                })
//                .toList();
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<InspectionResponse> getInspectionById(
//            @PathVariable UUID id) {
//
//        InspectionEntity inspection =
//                inspectionService.getInspectionById(id);
//
//        InspectionResponse response = new InspectionResponse();
//
//        response.setId(inspection.getId());
//        response.setAssetId(inspection.getAssetId().getId());
//        response.setInspectorUserId(
//                inspection.getInspectorUserId().getId()
//        );
//        response.setNotes(inspection.getNotes());
//        response.setInspectedAt(inspection.getInspectedAt());
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/asset/{assetId}")
//    public ResponseEntity<List<InspectionResponse>> getInspectionsByAssetId(
//            @PathVariable UUID assetId) {
//
//        List<InspectionEntity> inspections =
//                inspectionService.getInspectionsByAssetId(assetId);
//
//        List<InspectionResponse> responses = inspections.stream()
//                .map(inspection -> {
//
//                    InspectionResponse response = new InspectionResponse();
//
//                    response.setId(inspection.getId());
//                    response.setAssetId(inspection.getAssetId().getId());
//                    response.setInspectorUserId(inspection.getInspectorUserId().getId());
//                    response.setNotes(inspection.getNotes());
//                    response.setInspectedAt(inspection.getInspectedAt());
//
//                    return response;
//                })
//                .toList();
//
//        return ResponseEntity.ok(responses);
//    }
//
//}

package com.schoolerp.usermanagement.modules.inspection.controller;

import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
import com.schoolerp.usermanagement.modules.inspection.entity.InspectionPhotoEntity;
import com.schoolerp.usermanagement.modules.inspection.requestDto.AddInspectionPhotoRequest;
import com.schoolerp.usermanagement.modules.inspection.requestDto.CreateInspectionRequest;
import com.schoolerp.usermanagement.modules.inspection.responseDto.InspectionResponse;
import com.schoolerp.usermanagement.modules.inspection.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;


// =========================================================
// CREATE INSPECTION REPORT
// =========================================================

    @PostMapping
    public ResponseEntity<InspectionResponse> createInspection(
            @RequestBody CreateInspectionRequest request
    ) {

        InspectionEntity inspection =
                inspectionService.createInspection(
                        request.getAssetId(),
                        request.getInspectorUserId(),
                        request.getNotes(),
                        request.getLatitude(),
                        request.getLongitude()
                );

        return ResponseEntity.ok(
                convertToResponse(inspection)
        );
    }


// =========================================================
// ADD PHOTO TO INSPECTION
// =========================================================

    @PostMapping("/{inspectionId}/photos")
    public ResponseEntity<InspectionResponse> addPhoto(
            @PathVariable UUID inspectionId,
            @RequestBody AddInspectionPhotoRequest request
    ) {

        inspectionService.addPhoto(
                inspectionId,
                request.getPhotoUrl()
        );

        InspectionEntity inspection =
                inspectionService.getInspectionById(
                        inspectionId
                );

        return ResponseEntity.ok(
                convertToResponse(inspection)
        );
    }


// =========================================================
// GET ALL INSPECTIONS
// =========================================================

    @GetMapping
    public ResponseEntity<List<InspectionResponse>>
    getAllInspections() {

        List<InspectionResponse> responses =
                inspectionService.getAllInspections()
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }


// =========================================================
// GET INSPECTION BY ID
// =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<InspectionResponse>
    getInspectionById(
            @PathVariable UUID id
    ) {

        InspectionEntity inspection =
                inspectionService.getInspectionById(id);

        return ResponseEntity.ok(
                convertToResponse(inspection)
        );
    }


// =========================================================
// GET INSPECTIONS BY ASSET
// =========================================================

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<InspectionResponse>>
    getInspectionsByAssetId(
            @PathVariable UUID assetId
    ) {

        List<InspectionResponse> responses =
                inspectionService
                        .getInspectionsByAssetId(assetId)
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }


// =========================================================
// GET INSPECTIONS BY INSPECTOR
// =========================================================

    @GetMapping("/inspector/{inspectorUserId}")
    public ResponseEntity<List<InspectionResponse>>
    getInspectionsByInspectorId(
            @PathVariable UUID inspectorUserId
    ) {

        List<InspectionResponse> responses =
                inspectionService
                        .getInspectionsByInspectorId(
                                inspectorUserId
                        )
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }


// =========================================================
// GET PHOTOS
// =========================================================

    @GetMapping("/{inspectionId}/photos")
    public ResponseEntity<List<String>>
    getPhotos(
            @PathVariable UUID inspectionId
    ) {

        List<String> photos =
                inspectionService
                        .getPhotos(inspectionId)
                        .stream()
                        .map(InspectionPhotoEntity::getPhotoUrl)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(photos);
    }


// =========================================================
// ENTITY → RESPONSE
// =========================================================

    private InspectionResponse convertToResponse(
            InspectionEntity inspection
    ) {

        InspectionResponse response =
                new InspectionResponse();

        response.setId(inspection.getId());

        response.setAssetId(
                inspection.getAssetId().getId()
        );

        response.setInspectorUserId(
                inspection.getInspectorUserId().getId()
        );

        response.setNotes(
                inspection.getNotes()
        );

        response.setInspectedAt(
                inspection.getInspectedAt()
        );


// Geo-tag
        if (inspection.getGeoTag() != null) {

            response.setLongitude(
                    inspection.getGeoTag().getX()
            );

            response.setLatitude(
                    inspection.getGeoTag().getY()
            );
        }


// Photos
        List<String> photoUrls =
                inspectionService
                        .getPhotos(inspection.getId())
                        .stream()
                        .map(InspectionPhotoEntity::getPhotoUrl)
                        .collect(Collectors.toList());

        response.setPhotoUrls(photoUrls);

        return response;
    }
}