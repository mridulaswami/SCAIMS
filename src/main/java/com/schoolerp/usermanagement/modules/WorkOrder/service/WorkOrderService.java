package com.schoolerp.usermanagement.modules.WorkOrder.service;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.CreateWorkOrderRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.StatusChangeRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.CreateWorkOrderResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.GetAllWorkOrderGroupByStatusResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.StatusChangeResponseDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface WorkOrderService {

    CreateWorkOrderResponseDto createWorkOrder(CreateWorkOrderRequestDto request , UUID userId);

    StatusChangeResponseDto changeStatus(StatusChangeRequestDto requestDto);

    List<GetAllWorkOrderGroupByStatusResponseDto> getAllWorkOrderGroupByStatus(String token);
}