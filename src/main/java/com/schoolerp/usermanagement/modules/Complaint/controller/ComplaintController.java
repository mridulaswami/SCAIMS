package com.schoolerp.usermanagement.modules.Complaint.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.Complaint.requestDto.CreateComplaintRequestDto;
import com.schoolerp.usermanagement.modules.Complaint.responseDto.ComplaintResponseDto;
import com.schoolerp.usermanagement.modules.Complaint.responseDto.GetAllComplaintsResponseDto;
import com.schoolerp.usermanagement.modules.Complaint.service.ComplaintService;
import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> createComplaint(@Valid @ModelAttribute CreateComplaintRequestDto request, HttpServletRequest req) {

        log.info("Create complaint API request received | title={} | assetId={}", request.getTitle(), request.getAsset());

        String accestoken = extractRefreshToken(req);
        UUID userId = UUID.fromString(jwtTokenProvider.getUserIdFromJWT(accestoken));

        ComplaintResponseDto response = complaintService.createComplaint(request, userId);

        log.info("Create complaint API completed successfully | citizenId={} | title={}", userId, request.getTitle());

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
}
