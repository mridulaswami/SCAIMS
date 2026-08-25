package com.schoolerp.usermanagement.modules.WorkOrder.service.impl;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.Complaint.repository.ComplaintRepository;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderPhotoEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.repository.WorkOrderEntityRepository;
import com.schoolerp.usermanagement.modules.WorkOrder.repository.WorkOrderPhotoEntityRepository;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.CreateWorkOrderRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.requestDto.StatusChangeRequestDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.CreateWorkOrderResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.StatusChangeResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.service.WorkOrderService;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderEntityRepository workOrderRepository;
    private final ComplaintRepository complaintRepository;
    private final UserEntityRepository userEntityRepository;
    private final WorkOrderPhotoEntityRepository workOrderPhotoEntityRepository;

    @Override
    @Transactional
    public CreateWorkOrderResponseDto createWorkOrder(CreateWorkOrderRequestDto request) {

        log.info("Started creating Work Order for complaintId: {}", request.getComplaintId());

        // 1. Check Already Work Order Created
        Optional<WorkOrderEntity> existingWorkOrder = workOrderRepository.findByComplaintId(request.getComplaintId());

        if (existingWorkOrder.isPresent()) {
            throw new RuntimeException("Work Order already assigned with the inspector or already in progress");
        }

        // 2. Fetch Complaint
        ComplaintEntity complaint = complaintRepository.findById(request.getComplaintId()).orElseThrow(() -> new RuntimeException("Complaint not found with id: " + request.getComplaintId()));

        // 3. Fetch Inspector/User
        UserEntity inspector = userEntityRepository.findById(request.getInspectorId()).orElseThrow(() -> new RuntimeException("Inspector/User not found with id: " + request.getInspectorId()));

        // 4. Create Work Order
        WorkOrderEntity workOrder = WorkOrderEntity.builder().complaintId(complaint).inspectorId(inspector).priority(request.getPriority()).status(WorkOrderEntity.Status.ASSIGNED).dueDate(request.getDueDate()).build();

        // 5. Save Work Order
        WorkOrderEntity savedWorkOrder = workOrderRepository.save(workOrder);

        log.info("Work Order created successfully with id: {}", savedWorkOrder.getId());

        // 6. Update Complaint Status
        complaint.setStatus(ComplaintEntity.Status.INPROGESS);
        complaintRepository.save(complaint);

        log.info("Complaint status updated to INPROGESS for complaintId: {}", complaint.getId());

        // 7. Map Entity -> Response DTO
        return CreateWorkOrderResponseDto.builder().id(savedWorkOrder.getId()).complaintId(savedWorkOrder.getComplaintId().getId()).inspectorId(savedWorkOrder.getInspectorId().getId()).priority(savedWorkOrder.getPriority()).status(savedWorkOrder.getStatus()).dueDate(savedWorkOrder.getDueDate()).createdAt(savedWorkOrder.getCreatedAt()).build();
    }

    @Override
    @Transactional
    public StatusChangeResponseDto changeStatus(StatusChangeRequestDto requestDto) {

        log.info("Started changing status for Work Order: {}", requestDto.getId());

        // 1. Find Work Order
        WorkOrderEntity workOrder = workOrderRepository.findById(requestDto.getId()).orElseThrow(() -> new RuntimeException("Work Order not found or invalid Work Order Id: " + requestDto.getId()));


        // 2. Get Complaint
        ComplaintEntity complaint = workOrder.getComplaintId();

        if (complaint == null) {
            throw new RuntimeException("Complaint not found for Work Order: " + workOrder.getId());
        }

        // 3. Requested Status
        WorkOrderEntity.Status requestedStatus = requestDto.getStatus();

        // 4. Status Business Logic
        switch (requestedStatus) {

            // IN_PROGRESS
            case IN_PROGRESS -> {

                workOrder.setStatus(WorkOrderEntity.Status.IN_PROGRESS);

                log.info("Work Order {} status changed to IN_PROGRESS", workOrder.getId());
            }


            // RESOLVED
            case RESOLVED -> {

                // Work Report Required
                validateWorkReport(requestDto.getWorkReport());

                // At least one photo required
                validatePhotos(requestDto.getPhotos());

                // Change Work Order Status
                workOrder.setStatus(WorkOrderEntity.Status.RESOLVED);

                // Save Work Report
                workOrder.setWorkReport(requestDto.getWorkReport());

                // Complaint → COMPLETED
                complaint.setStatus(ComplaintEntity.Status.COMPLETED);

                complaintRepository.save(complaint);

                // Save Multiple Photos
                saveWorkOrderPhotos(requestDto.getPhotos(), workOrder);

                log.info("Work Order {} RESOLVED and Complaint {} COMPLETED", workOrder.getId(), complaint.getId());
            }

            // CLOSED
            case CLOSED -> {

                // Work Report Required
                validateWorkReport(requestDto.getWorkReport());

                // At least one photo required
                validatePhotos(requestDto.getPhotos());

                // Change Work Order Status
                workOrder.setStatus(WorkOrderEntity.Status.CLOSED);

                // Save Work Report
                workOrder.setWorkReport(requestDto.getWorkReport());

                // Complaint → REJECTED
                complaint.setStatus(ComplaintEntity.Status.REJECTED);

                complaintRepository.save(complaint);

                // Save Multiple Photos
                saveWorkOrderPhotos(requestDto.getPhotos(), workOrder);

                log.info("Work Order {} CLOSED and Complaint {} REJECTED", workOrder.getId(), complaint.getId());
            }


            // ASSIGNED
            case ASSIGNED -> {

                throw new RuntimeException("Work Order cannot be changed back to ASSIGNED");
            }

            default -> {

                throw new RuntimeException("Invalid Work Order status: " + requestedStatus);
            }
        }

        // 5. Save Work Order
        WorkOrderEntity savedWorkOrder = workOrderRepository.save(workOrder);


        // 6. Response
        return StatusChangeResponseDto.builder().status(savedWorkOrder.getStatus()).workReport(savedWorkOrder.getWorkReport()).build();
    }

    private void validateWorkReport(String workReport) {

        if (workReport == null || workReport.trim().isEmpty()) {

            throw new RuntimeException("Work report is required");
        }
    }

    private void validatePhotos(List<MultipartFile> photos) {

        if (photos == null || photos.isEmpty()) {

            throw new RuntimeException("At least one photo is required");
        }

        boolean hasValidPhoto = photos.stream().anyMatch(photo -> photo != null && !photo.isEmpty());

        if (!hasValidPhoto) {

            throw new RuntimeException("At least one valid photo is required");
        }
    }

    private void saveWorkOrderPhotos(List<MultipartFile> photos, WorkOrderEntity workOrder) {

        if (photos == null || photos.isEmpty()) {
            return;
        }

        for (MultipartFile photo : photos) {

            if (photo == null || photo.isEmpty()) {
                continue;
            }

            try {

                // Save physical file
                String photoUrl = saveWorkOrderPhoto(photo, workOrder.getId());

                // Save DB record
                WorkOrderPhotoEntity photoEntity = WorkOrderPhotoEntity.builder().workOrderId(workOrder).photoUrl(photoUrl).build();

                workOrderPhotoEntityRepository.save(photoEntity);

            } catch (IOException e) {

                log.error("Failed to save photo for Work Order: {}", workOrder.getId(), e);

                throw new RuntimeException("Failed to save Work Order photo", e);
            }
        }
    }

    private String saveWorkOrderPhoto(MultipartFile photo, UUID workOrderId) throws IOException {

        String uploadDirectory = "uploads/workOrder";

        // uploads/workOrder/{workOrderId}
        Path workOrderDirectory = Paths.get(uploadDirectory, workOrderId.toString());

        // Create directory
        Files.createDirectories(workOrderDirectory);

        // Original filename
        String originalFileName = photo.getOriginalFilename();

        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {

            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Unique filename
        String fileName = UUID.randomUUID() + extension;

        // Final path
        Path filePath = workOrderDirectory.resolve(fileName);

        // Save file
        Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL
        return "/uploads/workOrder/" + workOrderId + "/" + fileName;
    }
}