//package com.schoolerp.usermanagement.modules.inspection.controller;
//
//import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
//import com.schoolerp.usermanagement.modules.inspection.requestDto.CreateInspectionRequest;
//import com.schoolerp.usermanagement.modules.inspection.responseDto.responseDto.InspectionResponse;
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

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.StatusChangeRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.GetAllWorkOrderGroupByStatusResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.StatusChangeResponseDto;
import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
import com.schoolerp.usermanagement.modules.inspection.entity.InspectionPhotoEntity;
import com.schoolerp.usermanagement.modules.inspection.requestDto.AddInspectionPhotoRequest;
import com.schoolerp.usermanagement.modules.inspection.requestDto.CreateInspectionRequest;
import com.schoolerp.usermanagement.modules.inspection.requestDto.InspectorStatusChangeRequestDto;
import com.schoolerp.usermanagement.modules.inspection.responseDto.GetAllInspectionGroupByStatusResponseDto;
import com.schoolerp.usermanagement.modules.inspection.responseDto.InspectionResponse;
import com.schoolerp.usermanagement.modules.inspection.responseDto.InspectorStatusChangeResponseDto;
import com.schoolerp.usermanagement.modules.inspection.service.InspectionService;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
@Slf4j
public class InspectionController {

    private final InspectionService inspectionService;
    private final JwtTokenProvider jwtTokenProvider;



// =========================================================
// CREATE INSPECTION REPORT
// =========================================================

    @PostMapping
    public ResponseEntity<InspectionResponse> createInspection(
            @RequestBody CreateInspectionRequest request , HttpServletRequest req
    ) {

        String accestoken = extractRefreshToken(req);
        UUID userId = UUID.fromString(jwtTokenProvider.getUserIdFromJWT(accestoken));

        InspectionEntity inspection =
                inspectionService.createInspection(
                        request.getAssetId(),
                        request.getInspectorUserId(),
                        request.getNotes(), String.valueOf(request.getPriority()),
                        userId
                );

        return ResponseEntity.ok(
                convertToResponse(inspection)
        );
    }

    private String extractRefreshToken(HttpServletRequest request) {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new RuntimeException("Authorization header not found");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header");
        }

        String refreshToken = authorizationHeader.substring(7).trim();

        if (refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token not found");
        }

        return refreshToken;
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

    @GetMapping("/all")
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

    @PostMapping(value = "/change/status", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('FIELD_ENGINEER','ADMIN')")
    public ResponseEntity<ApiResponse<InspectorStatusChangeResponseDto>> changeStatus(@Valid @ModelAttribute InspectorStatusChangeRequestDto requestDto) {

        InspectorStatusChangeResponseDto response = inspectionService.changeStatus(requestDto);

        return null;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_ENGINEER')")
    public ResponseEntity<ApiResponse<List<GetAllInspectionGroupByStatusResponseDto>>> getAllInspectionGroupByStatus(HttpServletRequest request) {

        log.info("Get all Inspection started");

        String token = jwtTokenProvider.extractAccestoken(request);

        if (token == null || token.isEmpty()) {
            log.warn("Token is empty or null");
            throw new RuntimeException("Unauthorized: Token is missing");
        }

        List<GetAllInspectionGroupByStatusResponseDto> response = inspectionService.getAllInspectionGroupByStatus(token);

        log.info("Get all Inspection completed. Total records: {}", response.size());

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Fetched All Inspection successfully", response));
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
//        if (inspection.getGeoTag() != null) {
//
//            response.setLongitude(
//                    inspection.getGeoTag().getX()
//            );
//
//            response.setLatitude(
//                    inspection.getGeoTag().getY()
//            );
//        }


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