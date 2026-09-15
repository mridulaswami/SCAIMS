package com.schoolerp.usermanagement.modules.WorkOrder.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.CreateWorkOrderRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.StatusChangeRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.CreateWorkOrderResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.GetAllWorkOrderGroupByStatusResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.StatusChangeResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.service.WorkOrderService;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/workOrder")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreateWorkOrderResponseDto>> createWorkOrder(@Valid @ModelAttribute CreateWorkOrderRequestDto request , HttpServletRequest req) {

        log.info("Started creating Work Order with request: {}", request);

        String accestoken = extractRefreshToken(req);
        UUID userId = UUID.fromString(jwtTokenProvider.getUserIdFromJWT(accestoken));

        CreateWorkOrderResponseDto response = workOrderService.createWorkOrder(request , userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(true, "Work Order created successfully", response));
    }

    @PostMapping(value = "/change/status", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_ENGINEER')")
    public ResponseEntity<ApiResponse<StatusChangeResponseDto>> changeStatus(@Valid @ModelAttribute StatusChangeRequestDto requestDto) {

        log.info("Started change status of Work Order: {}", requestDto.getId());

        StatusChangeResponseDto response = workOrderService.changeStatus(requestDto);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Work Order status changed successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_ENGINEER')")
    public ResponseEntity<ApiResponse<List<GetAllWorkOrderGroupByStatusResponseDto>>> getAllWorkOrderGroupByStatus(HttpServletRequest request) {

        log.info("Get all Work Order started");

        String token = jwtTokenProvider.extractAccestoken(request);

        if (token == null || token.isEmpty()) {
            log.warn("Token is empty or null");
            throw new RuntimeException("Unauthorized: Token is missing");
        }

        List<GetAllWorkOrderGroupByStatusResponseDto> response = workOrderService.getAllWorkOrderGroupByStatus(token);

        log.info("Get all Work Order completed. Total records: {}", response.size());

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Fetched All Work Order successfully", response));
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