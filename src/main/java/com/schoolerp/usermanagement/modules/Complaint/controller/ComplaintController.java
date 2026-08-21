package com.schoolerp.usermanagement.modules.Complaint.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.Complaint.requestDto.CreateComplaintRequestDto;
import com.schoolerp.usermanagement.modules.Complaint.responseDto.ComplaintResponseDto;
import com.schoolerp.usermanagement.modules.Complaint.responseDto.GetAllComplaintsResponseDto;
import com.schoolerp.usermanagement.modules.Complaint.service.ComplaintService;
import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/complaints")
@RequiredArgsConstructor
@Slf4j
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> createComplaint(@Valid @ModelAttribute CreateComplaintRequestDto request) {

        log.info("Create complaint API request received | citizenId={} | title={} | assetId={}", request.getCitizenId(), request.getTitle(), request.getAsset());

        ComplaintResponseDto response = complaintService.createComplaint(request);

        log.info("Create complaint API completed successfully | citizenId={} | title={}", request.getCitizenId(), request.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(true, "Complaint Created Successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    public ResponseEntity<ApiResponse<List<GetAllComplaintsResponseDto>>> getAllComplaints(@RequestHeader("Authorization") String authorizationHeader) {

        log.info("Get all complaints API request received");

        String token = "";
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            log.info("Token Found {}", token);
        }

        List<GetAllComplaintsResponseDto> response = complaintService.getAllComplaints(token);

        log.info("Get all complaints API completed successfully | count={}", response.size());

        return ResponseEntity.ok(ApiResponse.of(true, "Complaints fetched successfully", response));
    }
}
