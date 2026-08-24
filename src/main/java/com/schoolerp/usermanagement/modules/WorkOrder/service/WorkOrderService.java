package com.schoolerp.usermanagement.modules.WorkOrder.service;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.CreateWorkOrderRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.StatusChangeRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.CreateWorkOrderResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.StatusChangeResponseDto;

public interface WorkOrderService {

    CreateWorkOrderResponseDto createWorkOrder(CreateWorkOrderRequestDto request);

//    StatusChangeResponseDto changeStatus(StatusChangeRequestDto requestDto);
}