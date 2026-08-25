package com.schoolerp.usermanagement.modules.WorkOrder.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.CreateWorkOrderRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.StatusChangeRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.CreateWorkOrderResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.StatusChangeResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/workOrder")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreateWorkOrderResponseDto>> createWorkOrder(@Valid @RequestBody CreateWorkOrderRequestDto request) {

        log.info("Started creating Work Order with request: {}", request);

        CreateWorkOrderResponseDto response = workOrderService.createWorkOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(true, "Work Order created successfully", response));
    }

    @PostMapping(value = "/change/status", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_ENGINEER')")
    public ResponseEntity<ApiResponse<StatusChangeResponseDto>> changeStatus(@Valid @ModelAttribute StatusChangeRequestDto requestDto) {

        log.info("Started change status of Work Order: {}", requestDto.getId());

        StatusChangeResponseDto response = workOrderService.changeStatus(requestDto);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Work Order status changed successfully", response));
    }
}