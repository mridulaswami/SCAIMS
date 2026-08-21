package com.schoolerp.usermanagement.modules.WorkOrder.service.impl;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.Complaint.repository.ComplaintRepository;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.repository.WorkOrderEntityRepository;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.CreateWorkOrderRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.CreateWorkOrderResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.service.WorkOrderService;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderEntityRepository workOrderRepository;
    private final ComplaintRepository complaintRepository;
    private final UserEntityRepository userEntityRepository;

    @Override
    @Transactional
    public CreateWorkOrderResponseDto createWorkOrder(CreateWorkOrderRequestDto request) {

        log.info("Started creating Work Order for complaintId: {}", request.getComplaintId());

        /*
         * 1. Check Already Work Order Created
         */

        Optional<WorkOrderEntity> existingWorkOrder = workOrderRepository.findByComplaintId(request.getComplaintId());

        if (existingWorkOrder.isPresent()) {
            throw new RuntimeException("Work Order already assigned with the inspector or already in progress");
        }
        /*
         * 2. Fetch Complaint
         */
        ComplaintEntity complaint = complaintRepository.findById(request.getComplaintId()).orElseThrow(() -> new RuntimeException("Complaint not found with id: " + request.getComplaintId()));

        /*
         * 3. Fetch Inspector/User
         */
        UserEntity inspector = userEntityRepository.findById(request.getInspectorId()).orElseThrow(() -> new RuntimeException("Inspector/User not found with id: " + request.getInspectorId()));

        /*
         * 4. Create Work Order
         */
        WorkOrderEntity workOrder = WorkOrderEntity.builder().complaintId(complaint).inspectorId(inspector).priority(request.getPriority()).status(WorkOrderEntity.Status.ASSIGNED).dueDate(request.getDueDate()).build();

        /*
         * 5. Save Work Order
         */
        WorkOrderEntity savedWorkOrder = workOrderRepository.save(workOrder);

        log.info("Work Order created successfully with id: {}", savedWorkOrder.getId());

        /*
         * 5. Map Entity -> Response DTO
         */
        return CreateWorkOrderResponseDto.builder().id(savedWorkOrder.getId()).complaintId(savedWorkOrder.getComplaintId().getId()).inspectorId(savedWorkOrder.getInspectorId().getId()).priority(savedWorkOrder.getPriority()).status(savedWorkOrder.getStatus()).dueDate(savedWorkOrder.getDueDate()).createdAt(savedWorkOrder.getCreatedAt()).build();
    }
}