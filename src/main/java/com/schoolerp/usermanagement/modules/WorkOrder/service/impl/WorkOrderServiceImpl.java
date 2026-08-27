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
import com.schoolerp.usermanagement.modules.audit.Repository.ComplaintStatusAuditRepository;
import com.schoolerp.usermanagement.modules.audit.Repository.WorkOrderStatusAuditRepository;
import com.schoolerp.usermanagement.modules.audit.entity.ComplaintStatusAuditEntity;
import com.schoolerp.usermanagement.modules.audit.entity.WorkOrderStatusAuditEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
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
    private final WorkOrderStatusAuditRepository workOrderStatusAuditRepository;
    private final ComplaintStatusAuditRepository complaintStatusAuditRepository;

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


        // 1. Validate Request
        if (requestDto == null) {
            throw new RuntimeException("Status change request cannot be null");
        }

        if (requestDto.getId() == null) {
            throw new RuntimeException("Work Order ID is required");
        }

        if (requestDto.getStatus() == null) {
            throw new RuntimeException("Work Order status is required");
        }

        log.info("Started changing status for Work Order: {} to {}", requestDto.getId(), requestDto.getStatus());


        // 2. Find Work Order
        WorkOrderEntity workOrder = workOrderRepository.findById(requestDto.getId()).orElseThrow(() -> new RuntimeException("Work Order not found or invalid Work Order Id: " + requestDto.getId()));


        // 3. Get Complaint
        ComplaintEntity complaint = workOrder.getComplaintId();

        if (complaint == null) {
            throw new RuntimeException("Complaint not found for Work Order: " + workOrder.getId());
        }


        // 4. Get Current and Requested Status
        WorkOrderEntity.Status previousWorkOrderStatus = workOrder.getStatus();

        WorkOrderEntity.Status requestedStatus = requestDto.getStatus();


        if (previousWorkOrderStatus == null) {
            throw new RuntimeException("Current Work Order status is not defined for Work Order: " + workOrder.getId());
        }


        // 5. Prevent Same Status
        if (previousWorkOrderStatus == requestedStatus) {
            throw new RuntimeException("Work Order is already in " + requestedStatus + " status");
        }


        // 6. Validate Status Transition
        validateWorkOrderStatusTransition(previousWorkOrderStatus, requestedStatus);


        // 7. Get Current Logged-in User

        UserEntity changedBy = getCurrentLoggedInUser();


        // 8. Status Business Logic
        switch (requestedStatus) {

            // =====================================================
            // IN_PROGRESS
            // =====================================================

            case IN_PROGRESS -> {

                workOrder.setStatus(WorkOrderEntity.Status.IN_PROGRESS);

                log.info("Work Order {} status changed from {} to IN_PROGRESS", workOrder.getId(), previousWorkOrderStatus);
            }


            // =====================================================
            // RESOLVED
            // =====================================================

            case RESOLVED -> {

                // Work Report Required
                validateWorkReport(requestDto.getWorkReport());

                // At least one valid photo required
                validatePhotos(requestDto.getPhotos());


                // Change Work Order Status
                workOrder.setStatus(WorkOrderEntity.Status.RESOLVED);


                // Save Work Report
                workOrder.setWorkReport(requestDto.getWorkReport());


                // =================================================
                // Complaint Status
                // =================================================

                ComplaintEntity.Status previousComplaintStatus = complaint.getStatus();

                ComplaintEntity.Status newComplaintStatus = ComplaintEntity.Status.COMPLETED;


                if (previousComplaintStatus == null) {
                    throw new RuntimeException("Current Complaint status is not defined for Complaint: " + complaint.getId());
                }


                if (previousComplaintStatus != newComplaintStatus) {

                    complaint.setStatus(newComplaintStatus);

                    complaintRepository.save(complaint);


                    // Complaint Audit
                    saveComplaintStatusAudit(complaint, previousComplaintStatus, newComplaintStatus, changedBy);
                }


                // =================================================
                // Work Order Photos
                // =================================================

                saveWorkOrderPhotos(requestDto.getPhotos(), workOrder);


                log.info("Work Order {} changed to RESOLVED and Complaint {} changed to COMPLETED", workOrder.getId(), complaint.getId());
            }


            // =====================================================
            // CLOSED
            // =====================================================

            case CLOSED -> {

                // Work Report Required
                validateWorkReport(requestDto.getWorkReport());

                // At least one valid photo required
                validatePhotos(requestDto.getPhotos());


                // Change Work Order Status
                workOrder.setStatus(WorkOrderEntity.Status.CLOSED);


                // Save Work Report
                workOrder.setWorkReport(requestDto.getWorkReport());


                // =================================================
                // Complaint Status
                // =================================================

                ComplaintEntity.Status previousComplaintStatus = complaint.getStatus();

                ComplaintEntity.Status newComplaintStatus = ComplaintEntity.Status.REJECTED;


                if (previousComplaintStatus == null) {
                    throw new RuntimeException("Current Complaint status is not defined for Complaint: " + complaint.getId());
                }


                if (previousComplaintStatus != newComplaintStatus) {

                    complaint.setStatus(newComplaintStatus);

                    complaintRepository.save(complaint);


                    // Complaint Audit
                    saveComplaintStatusAudit(complaint, previousComplaintStatus, newComplaintStatus, changedBy);
                }


                // =================================================
                // Work Order Photos
                // =================================================

                saveWorkOrderPhotos(requestDto.getPhotos(), workOrder);


                log.info("Work Order {} changed to CLOSED and Complaint {} changed to REJECTED", workOrder.getId(), complaint.getId());
            }


            // =====================================================
            // ASSIGNED
            // =====================================================

            case ASSIGNED -> {

                throw new RuntimeException("Work Order cannot be changed back to ASSIGNED");
            }


            // =====================================================
            // Invalid
            // =====================================================

            default -> {

                throw new RuntimeException("Invalid Work Order status: " + requestedStatus);
            }
        }


        // =========================================================
        // 9. Save Work Order
        // =========================================================

        WorkOrderEntity savedWorkOrder = workOrderRepository.save(workOrder);


        // =========================================================
        // 10. Work Order Audit
        // =========================================================

        saveWorkOrderStatusAudit(workOrder, previousWorkOrderStatus, requestedStatus, changedBy);


        // =========================================================
        // 11. Response
        // =========================================================

        return StatusChangeResponseDto.builder().status(savedWorkOrder.getStatus()).workReport(savedWorkOrder.getWorkReport()).build();
    }


    private void validateWorkOrderStatusTransition(WorkOrderEntity.Status currentStatus, WorkOrderEntity.Status requestedStatus) {

        if (currentStatus == null) {
            throw new RuntimeException("Current Work Order status cannot be null");
        }

        if (requestedStatus == null) {
            throw new RuntimeException("Requested Work Order status cannot be null");
        }

        if (currentStatus == requestedStatus) {
            throw new RuntimeException("Work Order is already in " + currentStatus + " status");
        }


        switch (currentStatus) {

            case ASSIGNED -> {

                // ASSIGNED can only move to IN_PROGRESS
                if (requestedStatus != WorkOrderEntity.Status.IN_PROGRESS) {

                    throw new RuntimeException("Work Order with ASSIGNED status can only be changed to IN_PROGRESS");
                }
            }


            case IN_PROGRESS -> {

                // IN_PROGRESS can move to RESOLVED or CLOSED
                if (requestedStatus != WorkOrderEntity.Status.RESOLVED && requestedStatus != WorkOrderEntity.Status.CLOSED) {

                    throw new RuntimeException("Work Order with IN_PROGRESS status can only be changed to RESOLVED or CLOSED");
                }
            }


            case RESOLVED -> {

                // RESOLVED cannot move back
                throw new RuntimeException("Work Order with RESOLVED status cannot be changed again");
            }


            case CLOSED -> {

                // CLOSED is final
                throw new RuntimeException("Work Order with CLOSED status cannot be changed again");
            }


            default -> {

                throw new RuntimeException("Invalid current Work Order status: " + currentStatus);
            }
        }
    }


    private void saveWorkOrderStatusAudit(WorkOrderEntity workOrder, WorkOrderEntity.Status previousStatus, WorkOrderEntity.Status currentStatus, UserEntity changedBy) {

        if (workOrder == null) {
            throw new RuntimeException("Work Order cannot be null while creating status audit");
        }

        if (previousStatus == null) {
            throw new RuntimeException("Previous Work Order status cannot be null");
        }

        if (currentStatus == null) {
            throw new RuntimeException("Current Work Order status cannot be null");
        }

        if (previousStatus == currentStatus) {
            throw new RuntimeException("Previous and current Work Order status cannot be same");
        }

        if (changedBy == null) {
            throw new RuntimeException("Changed By user cannot be null");
        }


        WorkOrderStatusAuditEntity audit = WorkOrderStatusAuditEntity.builder().workOrderId(workOrder).previousStatus(WorkOrderStatusAuditEntity.Status.valueOf(currentStatus.name())).currentStatus(WorkOrderStatusAuditEntity.Status.valueOf(currentStatus.name())).changeAt(LocalDateTime.now()).changedBy(changedBy).build();


        // IMPORTANT:
        // Previous status must represent the actual previous status.
        audit.setPreviousStatus(WorkOrderStatusAuditEntity.Status.valueOf(previousStatus.name()));


        workOrderStatusAuditRepository.save(audit);


        log.info("Work Order status audit saved | workOrderId={} | previousStatus={} | currentStatus={} | changedBy={}", workOrder.getId(), previousStatus, currentStatus, changedBy.getId());
    }


    private void saveComplaintStatusAudit(ComplaintEntity complaint, ComplaintEntity.Status previousStatus, ComplaintEntity.Status currentStatus, UserEntity changedBy) {

        if (complaint == null) {
            throw new RuntimeException("Complaint cannot be null while creating status audit");
        }

        if (previousStatus == null) {
            throw new RuntimeException("Previous Complaint status cannot be null");
        }

        if (currentStatus == null) {
            throw new RuntimeException("Current Complaint status cannot be null");
        }

        if (previousStatus == currentStatus) {
            throw new RuntimeException("Previous and current Complaint status cannot be same");
        }

        if (changedBy == null) {
            throw new RuntimeException("Changed By user cannot be null");
        }


        ComplaintStatusAuditEntity audit = ComplaintStatusAuditEntity.builder().complaintId(complaint).previousStatus(ComplaintStatusAuditEntity.Status.valueOf(previousStatus.name())).currentStatus(ComplaintStatusAuditEntity.Status.valueOf(currentStatus.name())).changeAt(LocalDateTime.now()).changedBy(changedBy).build();


        complaintStatusAuditRepository.save(audit);


        log.info("Complaint status audit saved | complaintId={} | previousStatus={} | currentStatus={} | changedBy={}", complaint.getId(), previousStatus, currentStatus, changedBy.getId());
    }


    private UserEntity getCurrentLoggedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {

            throw new RuntimeException("Authenticated user not found");
        }


        String username = authentication.getName();


        if (username == null || username.trim().isEmpty()) {

            throw new RuntimeException("Authenticated username not found");
        }


        return userEntityRepository.findByUserName(username).orElseThrow(() -> new RuntimeException("User not found for authenticated username: " + username));
    }


    private void validateWorkReport(String workReport) {

        if (workReport == null || workReport.trim().isEmpty()) {

            throw new RuntimeException("Work report is required");
        }

        if (workReport.trim().length() < 10) {

            throw new RuntimeException("Work report must contain at least 10 characters");
        }
    }


    private void validatePhotos(List<MultipartFile> photos) {

        if (photos == null || photos.isEmpty()) {

            throw new RuntimeException("At least one photo is required");
        }


        boolean hasValidPhoto = photos.stream().anyMatch(photo -> photo != null && !photo.isEmpty() && photo.getSize() > 0);


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

