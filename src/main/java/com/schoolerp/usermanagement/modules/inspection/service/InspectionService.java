package com.schoolerp.usermanagement.modules.inspection.service;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.Geometry.GeometryService;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderPhotoEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.GetAllWorkOrderGroupByStatusResponseDto;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.StatusChangeResponseDto;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.audit.Repository.InspectionStatusAuditRepository;
import com.schoolerp.usermanagement.modules.audit.entity.InspectionStatusAuditEntity;
import com.schoolerp.usermanagement.modules.audit.entity.WorkOrderStatusAuditEntity;
import com.schoolerp.usermanagement.modules.email.constant.EmailSubjectConstant;
import com.schoolerp.usermanagement.modules.email.constant.EmailTemplateConstant;
import com.schoolerp.usermanagement.modules.email.requestDto.EmailRequestDto;
import com.schoolerp.usermanagement.modules.email.service.EmailService;
import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
import com.schoolerp.usermanagement.modules.inspection.entity.InspectionPhotoEntity;
import com.schoolerp.usermanagement.modules.inspection.repository.InspectionPhotoRepository;
import com.schoolerp.usermanagement.modules.inspection.repository.InspectionRepository;
import com.schoolerp.usermanagement.modules.inspection.requestDto.InspectorStatusChangeRequestDto;
import com.schoolerp.usermanagement.modules.inspection.responseDto.GetAllInspectionGroupByStatusResponseDto;
import com.schoolerp.usermanagement.modules.inspection.responseDto.InspectorStatusChangeResponseDto;
import com.schoolerp.usermanagement.modules.notification.constant.NotificationTitleConstant;
import com.schoolerp.usermanagement.modules.notification.enums.NotificationPriority;
import com.schoolerp.usermanagement.modules.notification.enums.NotificationType;
import com.schoolerp.usermanagement.modules.notification.enums.TargetType;
import com.schoolerp.usermanagement.modules.notification.event.NotificationEventPublisher;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.ThreadSafe;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final AssetRepository assetRepository;
    private final UserEntityRepository userEntityRepository;
    private final InspectionPhotoRepository inspectionPhotoRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GeometryService geometryService;
    private final GeometryService geometryConverter;
    private final EmailService emailService;
    private final NotificationEventPublisher notificationEventPublisher;
    private final InspectionStatusAuditRepository inspectionStatusAuditRepository;

    @Transactional
    public InspectionEntity createInspection(
            UUID assetId,
            UUID inspectorUserId,
            String notes, String priority,
           UUID created_by
    ){
//        if (latitude == null || longitude == null){
//            throw new IllegalArgumentException("Latitude and longitude are required");
//        }
//        if (latitude < -90 || latitude > 90) {
//            throw new IllegalArgumentException(
//                    "Invalid latitude"
//            );
//        }
//
//        if (longitude < -180 || longitude > 180) {
//            throw new IllegalArgumentException(
//                    "Invalid longitude"
//            );
//        }
//        if (geoTag == null){
//            throw new IllegalArgumentException("Geotag required");
//        }
        AssetEntity asset = assetRepository.findById(assetId)
                .orElseThrow(() ->
                        new RuntimeException("Asset not found:" + assetId)
                );

        UserEntity inspector = userEntityRepository.findById(inspectorUserId)
                .orElseThrow(()->
                        new RuntimeException("Inspector not found:" + inspectorUserId)
                );

        UserEntity admin = userEntityRepository.findById(created_by)
                .orElseThrow(()->
                        new RuntimeException("Admin not found:" + created_by)
                );

        GeometryFactory geometryFactory = new GeometryFactory();

//        Point geoTag = geometryFactory.createPoint(
//                new Coordinate(longitude, latitude)
//        );
//        geoTag.setSRID(4326);


        InspectionEntity inspection = InspectionEntity.builder()
                .assetId(asset)
                .inspectorUserId(inspector)
                .notes(notes).createdBy(admin).status(InspectionEntity.Status.ASSIGNED)
                .priority(InspectionEntity.Priority.valueOf(priority))
              //  .geoTag(geoTag)
                .build();

        InspectionEntity savedInspection = inspectionRepository.save(inspection);

        asset.setLastInspectionDate(LocalDateTime.now());

        assetRepository.save(asset);

        return savedInspection;
    }

    @Transactional
    public InspectorStatusChangeResponseDto changeStatus(InspectorStatusChangeRequestDto requestDto){

        {


            // 1. Validate Request
            if (requestDto == null) {
                throw new RuntimeException("Status change request cannot be null");
            }

            if (requestDto.getId() == null) {
                throw new RuntimeException("Inspection ID is required");
            }

            if (requestDto.getStatus() == null) {
                throw new RuntimeException("Inspection status is required");
            }

            log.info("Started changing status for Inspection: {} to {}", requestDto.getId(), requestDto.getStatus());


            // 2. Find Inspection
            InspectionEntity inspection = inspectionRepository.findById(requestDto.getId()).orElseThrow(() -> new RuntimeException("Inspection not found or invalid Id: " + requestDto.getId()));


//            // 3. Get Complaint
//            ComplaintEntity complaint = workOrder.getComplaintId();
//
//            if (complaint == null) {
//                throw new RuntimeException("Complaint not found for Inspection: " + workOrder.getId());
//            }

    //        UserEntity citizen = userEntityRepository.findById(complaint.getCitizenId().getId()).orElseThrow(()-> new RuntimeException("Citizen not found"));

            UserEntity inspector = userEntityRepository.findById(inspection.getInspectorUserId().getId()).orElseThrow(()-> new RuntimeException("Inspector not found"));

            UserEntity adminUser = userEntityRepository.findById(inspection.getCreatedBy().getId()).orElseThrow(()-> new RuntimeException("Admin not found"));

            // 4. Get Current and Requested Status
            InspectionEntity.Status previousInspectionStatus = inspection.getStatus();

            InspectionEntity.Status requestedStatus = requestDto.getStatus();


            if (previousInspectionStatus == null) {
                throw new RuntimeException("Current Inspection status is not defined for Inspection: " + inspection.getId());
            }


            // 5. Prevent Same Status
            if (previousInspectionStatus == requestedStatus) {
                throw new RuntimeException("Inspection is already in " + requestedStatus + " status");
            }


            // 6. Validate Status Transition
            validateInspectionStatusTransition(previousInspectionStatus, requestedStatus);


            // 7. Get Current Logged-in User

            UserEntity changedBy = getCurrentLoggedInUser();


            // 8. Status Business Logic
            switch (requestedStatus) {

                // =====================================================
                // IN_PROGRESS
                // =====================================================

                case IN_PROGRESS -> {

                    inspection.setStatus(InspectionEntity.Status.IN_PROGRESS);

                    // transition mail to admin


                    try {

                        Map<String, Object> adminVariables = new HashMap<>();

                        adminVariables.put("inspectionId", inspection.getId());
                        adminVariables.put("lastInspection", inspection.getInspectedAt());
                        adminVariables.put("assignedTo", inspector.getUserName());
                        adminVariables.put("priority", inspection.getPriority());
                        adminVariables.put("dueDate", inspection.getDueDate());
                        adminVariables.put("InspectionStatus", inspection.getStatus());
                        adminVariables.put("assetName", inspection.getAssetId());
                        //   citizenVariables.put("assetLocation", assetLocation);

                        EmailRequestDto adminEmail = EmailRequestDto.builder().to(adminUser.getEmail()).subject(EmailSubjectConstant.ADMIN_STATUS_TRANSITION_NOTIFICATION).template(EmailTemplateConstant.ADMIN_STATUS_TRANSITION_NOTIFICATION).variables(adminVariables).build();

                        emailService.sendEmail(adminEmail);

                    } catch (Exception e) {

                        log.error("Failed to send Status Transition email to Admin | InspectionId={}", inspection.getId(), e);
                    }

                    // In-app notification to Admin
                    try {
                        notificationEventPublisher.publishToUser(
                                adminUser.getId(),
                                inspector.getId(),
                                NotificationTitleConstant.WORK_ORDER_IN_PROGRESS,
                                "Field engineer " + inspector.getName() + " has started work on Inspection #" + inspection.getId() + ".",
                                NotificationType.WORK_ORDER_STATUS_CHANGED,
                                NotificationPriority.MEDIUM,
                                TargetType.WORK_ORDER,
                                inspection.getId().toString()
                        );
                    } catch (Exception ex) {
                        log.error("Failed to send in-app notification to admin for Inspection in-progress: {}", ex.getMessage(), ex);
                    }

                }



                // =====================================================
                // RESOLVED
                // =====================================================

                case RESOLVED -> {

                    // Work Report Required
                    validateWorkReport(requestDto.getWorkReport());

                    // At least one valid photo required
                    validatePhotos(requestDto.getPhotos());


                    // Change Inspection Status
                    inspection.setStatus(InspectionEntity.Status.RESOLVED);

                    // send mail to inspector/admin about workorder status


                    // Save Work Report
                    inspection.setWorkReport(requestDto.getWorkReport());


                    // =================================================
                    // Complaint Status
                    // =================================================

//                    ComplaintEntity.Status previousComplaintStatus = complaint.getStatus();
//
//                    ComplaintEntity.Status newComplaintStatus = ComplaintEntity.Status.INPROGESS;

                    // send transition mail to admin to check if closed or re-assigned

                    try {

                        Map<String, Object> adminVariables = new HashMap<>();

                        adminVariables.put("inspectionId", inspection.getId());
                        adminVariables.put("lastInspection", inspection.getInspectedAt());
                        adminVariables.put("assignedTo", inspector.getUserName());
                        adminVariables.put("priority", inspection.getPriority());
                        adminVariables.put("dueDate", inspection.getDueDate());
                        adminVariables.put("InspectionStatus", inspection.getStatus());
                        adminVariables.put("assetName", inspection.getAssetId());

                        EmailRequestDto adminEmail = EmailRequestDto.builder().to(adminUser.getEmail()).subject(EmailSubjectConstant.ADMIN_STATUS_TRANSITION_NOTIFICATION).template(EmailTemplateConstant.ADMIN_STATUS_RESOLVED_NOTIFICATION).variables(adminVariables).build();

                        emailService.sendEmail(adminEmail);

                    } catch (Exception e) {

                        log.error("Failed to send Admin Resolved email | InspectionId={}", inspection.getId(), e);
                    }


//                    if (previousComplaintStatus == null) {
//                        throw new RuntimeException("Current Complaint status is not defined for Complaint: " + complaint.getId());
//                    }


//                    if (previousComplaintStatus != newComplaintStatus) {
//
//                        complaint.setStatus(newComplaintStatus);
//
//                        complaintRepository.save(complaint);
//
//
//                        // Complaint Audit
//                        saveComplaintStatusAudit(complaint, previousComplaintStatus, newComplaintStatus, changedBy);
//                    }


                    // =================================================
                    // Inspection Photos
                    // =================================================

                    saveInspectionPhotos(requestDto.getPhotos(), inspection);

                    // In-app notification to Admin for review
                    try {
                        notificationEventPublisher.publishToUser(
                                adminUser.getId(),
                                inspector.getId(),
                                NotificationTitleConstant.WORK_ORDER_RESOLVED_ADMIN,
                                "Inspection #" + inspection.getId() + " has been marked RESOLVED by " + inspector.getName() + ". Please review the work report and verify.",
                                NotificationType.WORK_ORDER_STATUS_CHANGED,
                                NotificationPriority.HIGH,
                                TargetType.WORK_ORDER,
                                inspection.getId().toString()
                        );
                    } catch (Exception ex) {
                        log.error("Failed to send in-app notification to admin for resolved Inspection: {}", ex.getMessage(), ex);
                    }


                    log.info("Inspection {} changed to RESOLVED ", inspection.getId());
                }



                // =====================================================
                // CLOSED
                // =====================================================

                case CLOSED -> {

                    // Work Report Required
                    validateWorkReport(requestDto.getWorkReport());

                    // At least one valid photo required
                    validatePhotos(requestDto.getPhotos());


                    // Change Inspection Status
                    inspection.setStatus(InspectionEntity.Status.CLOSED);



                    // Save Work Report
                    inspection.setWorkReport(requestDto.getWorkReport());

                    // send mail to admin about workorder status

                    try {

                        Map<String, Object> adminVariables = new HashMap<>();

                        adminVariables.put("inspectionId", inspection.getId());
                        adminVariables.put("lastInspection", inspection.getInspectedAt());
                        adminVariables.put("assignedTo", inspector.getUserName());
                        adminVariables.put("priority", inspection.getPriority());
                        adminVariables.put("dueDate", inspection.getDueDate());
                        adminVariables.put("InspectionStatus", inspection.getStatus());
                        adminVariables.put("assetName", inspection.getAssetId());
                        EmailRequestDto adminEmail = EmailRequestDto.builder().to(adminUser.getEmail()).subject(EmailSubjectConstant.ADMIN_STATUS_TRANSITION_NOTIFICATION).template(EmailTemplateConstant.ADMIN_STATUS_TRANSITION_NOTIFICATION).variables(adminVariables).build();

                        emailService.sendEmail(adminEmail);

                    } catch (Exception e) {

                        log.error("Failed to send Admin inspection Status email | inspectionId={}", inspection.getId(), e);
                    }



                    // =================================================
                    // Complaint Status
                    // =================================================

//                    ComplaintEntity.Status previousComplaintStatus = complaint.getStatus();
//
//                    ComplaintEntity.Status newComplaintStatus = ComplaintEntity.Status.COMPLETED;


                    // send complaint completed status mail to citizen

//                    try {
//
//                        Map<String, Object> citizenVariables = new HashMap<>();
//
//                        citizenVariables.put("complaintId", workOrder.getComplaintId().getId());
//                        citizenVariables.put("complaintTitle", workOrder.getComplaintId().getTitle());
//                        citizenVariables.put("complaintDescription", workOrder.getComplaintId().getDescription());
//                        citizenVariables.put("newComplaintStatus", complaint.getStatus());
//                        citizenVariables.put("workOrderId", workOrder.getId());
//                        citizenVariables.put("workOrderCreation", workOrder.getCreatedAt());
//                        citizenVariables.put("assignedTo", inspector.getUserName());
//                        citizenVariables.put("priority", workOrder.getPriority());
//                        citizenVariables.put("dueDate", workOrder.getDueDate());
//                        citizenVariables.put("workOrderStatus", workOrder.getStatus());
//                        citizenVariables.put("citizenName", citizen.getUserName());
//                        citizenVariables.put("assetName", workOrder.getComplaintId().getAsset().getName());
//
//                        EmailRequestDto citizenComplaintEmail = EmailRequestDto.builder().to(citizen.getEmail()).subject(EmailSubjectConstant.CITIZEN_WORKORDER_NOTIFICATION).template(EmailTemplateConstant.CITIZEN_WORKORDER_NOTIFICATION).variables(citizenVariables).build();
//
//                        emailService.sendEmail(citizenComplaintEmail);
//
//                    } catch (Exception e) {
//
//                        log.error("Failed to send Citizen workOrder email | workOrderId={}", workOrder.getId(), e);
//                    }

                    // send the complaint completed mail to inspector- ur insp has been closed

                    try {

                        Map<String, Object> inspectorVariables = new HashMap<>();

                        inspectorVariables.put("inspectionId", inspection.getId());
                        inspectorVariables.put("lastInspection", inspection.getInspectedAt());
                        inspectorVariables.put("assignedTo", inspector.getUserName());
                        inspectorVariables.put("priority", inspection.getPriority());
                        inspectorVariables.put("dueDate", inspection.getDueDate());
                        inspectorVariables.put("inspectionStatus", inspection.getStatus());
                        inspectorVariables.put("assetName", inspection.getAssetId());
                        EmailRequestDto inspectorEmail = EmailRequestDto.builder().to(inspector.getEmail()).subject(EmailSubjectConstant.INSPECTOR_STATUS_TRANSITION_NOTIFICATION).template(EmailTemplateConstant.INSPECTOR_STATUS_CLOSED_NOTIFICATION).variables(inspectorVariables).build();

                        emailService.sendEmail(inspectorEmail);

                    } catch (Exception e) {

                        log.error("Failed to send Inspector inspection Closed email | InspectionId={}", inspection.getId(), e);
                    }



//                    if (previousComplaintStatus == null) {
//                        throw new RuntimeException("Current Complaint status is not defined for Complaint: " + complaint.getId());
//                    }
//
//
//                    if (previousComplaintStatus != newComplaintStatus) {
//
//                        complaint.setStatus(newComplaintStatus);
//
//                        complaintRepository.save(complaint);
//
//
//                        // Complaint Audit
//                        saveComplaintStatusAudit(complaint, previousComplaintStatus, newComplaintStatus, changedBy);
//                    }


                    // =================================================
                    // Inspection Photos
                    // =================================================

                 saveInspectionPhotos(requestDto.getPhotos(), inspection);
//
//                    // In-app notification to Citizen
//                    try {
//                        notificationEventPublisher.publishToUser(
//                                citizen.getId(),
//                                changedBy != null ? changedBy.getId() : null,
//                                NotificationTitleConstant.COMPLAINT_RESOLVED,
//                                "Your complaint #" + complaint.getId() + " has been officially completed and closed. Thank you!",
//                                NotificationType.COMPLAINT_RESOLVED,
//                                NotificationPriority.HIGH,
//                                TargetType.COMPLAINT,
//                                complaint.getId().toString()
//                        );
//                    } catch (Exception ex) {
//                        log.error("Failed to send in-app notification to citizen for closed complaint: {}", ex.getMessage(), ex);
//                    }

                    // In-app notification to Inspector
                    try {
                        notificationEventPublisher.publishToUser(
                                inspector.getId(),
                                changedBy != null ? changedBy.getId() : null,
                                NotificationTitleConstant.WORK_ORDER_CLOSED_INSPECTOR,
                                "Your work report for Inspection #" + inspection.getId() + " was approved and the Inspection is now closed.",
                                NotificationType.WORK_ORDER_COMPLETED,
                                NotificationPriority.MEDIUM,
                                TargetType.WORK_ORDER,
                                inspection.getId().toString()
                        );
                    } catch (Exception ex) {
                        log.error("Failed to send in-app notification to inspector for closed Inspection: {}", ex.getMessage(), ex);
                    }

                    log.info("Inspection {} changed to CLOSED", inspection.getId(), inspection.getId());
                }



                // =====================================================
                // ASSIGNED
                // =====================================================

                case ASSIGNED -> {

                    String inspectorReport = inspection.getWorkReport();

                    //    validateWorkReport(requestDto.getRejectionReason());


                    // Change Inspection Status
                    inspection.setStatus(InspectionEntity.Status.ASSIGNED);


                    // Save Rejection Report
                    //    workOrder.setWorkReport(requestDto.getWorkReport());
                    inspection.setRejectionReason(requestDto.getRejectionReason());


                    // =================================================
                    // Complaint Status
                    // =================================================
//
//                    ComplaintEntity.Status previousComplaintStatus = complaint.getStatus();
//
//                    ComplaintEntity.Status newComplaintStatus = ComplaintEntity.Status.INPROGESS;


                    // sent mail to inspector that your insp is rejected and assigned to you again

                    try {

                        Map<String, Object> inspectorVariables = new HashMap<>();

                        inspectorVariables.put("inspectionId", inspection.getId());
                        inspectorVariables.put("lastInspection", inspection.getInspectedAt());
                        inspectorVariables.put("assignedTo", inspector.getUserName());
                        inspectorVariables.put("priority", inspection.getPriority());
                        inspectorVariables.put("dueDate", inspection.getDueDate());
                        inspectorVariables.put("inspectionStatus", inspection.getStatus());
                        inspectorVariables.put("assetName", inspection.getAssetId());
                        EmailRequestDto inspectorEmail = EmailRequestDto.builder().to(inspector.getEmail()).subject(EmailSubjectConstant.INSPECTOR_STATUS_TRANSITION_NOTIFICATION).template(EmailTemplateConstant.INSPECTOR_STATUS_REJECTED_NOTIFICATION).variables(inspectorVariables).build();

                        emailService.sendEmail(inspectorEmail);

                    } catch (Exception e) {

                        log.error("Failed to send Inspector Inspection Rejection email | InspectionId ={}", inspection.getId(), e);
                    }

                    // sent mail to admin that u have rejected the inspection.

                    try {

                        Map<String, Object> adminVariables = new HashMap<>();

                        adminVariables.put("inspectionId", inspection.getId());
                        adminVariables.put("lastInspection", inspection.getInspectedAt());
                        adminVariables.put("assignedTo", inspector.getUserName());
                        adminVariables.put("priority", inspection.getPriority());
                        adminVariables.put("dueDate", inspection.getDueDate());
                        adminVariables.put("InspectionStatus", inspection.getStatus());
                        adminVariables.put("assetName", inspection.getAssetId());
                        EmailRequestDto adminEmail = EmailRequestDto.builder().to(adminUser.getEmail()).subject(EmailSubjectConstant.ADMIN_STATUS_TRANSITION_NOTIFICATION).template(EmailTemplateConstant.ADMIN_STATUS_TRANSITION_NOTIFICATION).variables(adminVariables).build();

                        emailService.sendEmail(adminEmail);

                    } catch (Exception e) {

                        log.error("Failed to send Admin Inspection Status email | InspectionId={}", inspection.getId(), e);
                    }

                    saveInspectionPhotos(requestDto.getPhotos(), inspection);

                    // In-app notification to Inspector
                    try {
                        String reason = requestDto.getRejectionReason() != null ? requestDto.getRejectionReason() : "Resolution not approved";
                        notificationEventPublisher.publishToUser(
                                inspector.getId(),
                                changedBy != null ? changedBy.getId() : null,
                                NotificationTitleConstant.WORK_ORDER_REASSIGNED_INSPECTOR,
                                "Inspection #" + inspection.getId() + " was rejected by admin. Reason: " + reason + ". Please re-inspect and resolve.",
                                NotificationType.WORK_ORDER_STATUS_CHANGED,
                                NotificationPriority.URGENT,
                                TargetType.WORK_ORDER,
                                inspection.getId().toString()
                        );
                    } catch (Exception ex) {
                        log.error("Failed to send in-app notification to inspector for rejected Inspection: {}", ex.getMessage(), ex);
                    }

                }



                // =====================================================
                // Invalid
                // =====================================================

                default -> {

                    throw new RuntimeException("Invalid Inspection status: " + requestedStatus);
                }
            }


            // =========================================================
            // 9. Save Inspection
            // =========================================================

            InspectionEntity savedInspection = inspectionRepository.save(inspection);


            // =========================================================
            // 10. Inspection Audit
            // =========================================================

            saveInspectionStatusAudit(inspection, previousInspectionStatus, requestedStatus, changedBy);


            // =========================================================
            // 11. Response
            // =========================================================

            return InspectorStatusChangeResponseDto.builder().status(savedInspection.getStatus()).workReport(savedInspection.getWorkReport()).build();
        }
    }

    private void saveInspectionStatusAudit(InspectionEntity inspection, InspectionEntity.Status previousStatus, InspectionEntity.Status currentStatus, UserEntity changedBy) {

        if (inspection == null) {
            throw new RuntimeException("Inspection cannot be null while creating status audit");
        }

        if (previousStatus == null) {
            throw new RuntimeException("Previous Inspection status cannot be null");
        }

        if (currentStatus == null) {
            throw new RuntimeException("Current Inspection status cannot be null");
        }

        if (previousStatus == currentStatus) {
            throw new RuntimeException("Previous and current Inspection status cannot be same");
        }

        if (changedBy == null) {
            throw new RuntimeException("Changed By user cannot be null");
        }


        InspectionStatusAuditEntity audit = InspectionStatusAuditEntity.builder().inspectionId(inspection).previousStatus(InspectionStatusAuditEntity.Status.valueOf(currentStatus.name())).currentStatus(InspectionStatusAuditEntity.Status.valueOf(currentStatus.name())).changeAt(LocalDateTime.now()).changedBy(changedBy).build();


        // IMPORTANT:
        // Previous status must represent the actual previous status.
        audit.setPreviousStatus(InspectionStatusAuditEntity.Status.valueOf(previousStatus.name()));


        inspectionStatusAuditRepository.save(audit);


        log.info("Inspection status audit saved | InspectionId={} | previousStatus={} | currentStatus={} | changedBy={}", inspection.getId(), previousStatus, currentStatus, changedBy.getId());
    }

    @Transactional
    public InspectionPhotoEntity addPhoto(
            UUID inspectionId,
            String photoUrl
    ) {

        if (photoUrl == null || photoUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Photo URL is required"
            );
        }

        InspectionEntity inspection =
                inspectionRepository.findById(inspectionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inspection not found: "
                                                + inspectionId
                                )
                        );


        InspectionPhotoEntity photo =
                InspectionPhotoEntity.builder()
                        .inspection(inspection)
                        .photoUrl(photoUrl)
                        .build();


        return inspectionPhotoRepository.save(photo);
    }
    public List<InspectionEntity> getAllInspections(){
        return inspectionRepository.findAll();
    }

    public InspectionEntity getInspectionById(UUID id){
        return inspectionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Inspection Not Found: " + id)
                );
    }
    public List<InspectionEntity> getInspectionsByAssetId(UUID assetId){
        return inspectionRepository.findByAssetId_Id(assetId);
    }

    public List<InspectionEntity> getInspectionsByInspectorId(
            UUID inspectorUserId ){
        return inspectionRepository.findByInspectorUserId_Id(inspectorUserId);
    }
    public List<InspectionPhotoEntity> getPhotos(
            UUID inspectionId) {
        return inspectionPhotoRepository.findByInspection_Id(inspectionId);
    }

    private void validateInspectionStatusTransition(InspectionEntity.Status currentStatus, InspectionEntity.Status requestedStatus) {

        if (currentStatus == null) {
            throw new RuntimeException("Current Inspection status cannot be null");
        }

        if (requestedStatus == null) {
            throw new RuntimeException("Requested Inspection status cannot be null");
        }

        if (currentStatus == requestedStatus) {
            throw new RuntimeException("Inspection is already in " + currentStatus + " status");
        }


        switch (currentStatus) {

            case ASSIGNED -> {

                // ASSIGNED can only move to IN_PROGRESS
                if (requestedStatus != InspectionEntity.Status.IN_PROGRESS) {

                    throw new RuntimeException("Inspection with ASSIGNED status can only be changed to IN_PROGRESS");
                }
            }


            case IN_PROGRESS -> {

                // IN_PROGRESS can move to RESOLVED or CLOSED
                if (requestedStatus != InspectionEntity.Status.RESOLVED && requestedStatus != InspectionEntity.Status.CLOSED) {

                    throw new RuntimeException("Inspection with IN_PROGRESS status can only be changed to RESOLVED or CLOSED");
                }
            }


            case RESOLVED -> {

                // RESOLVED cannot move back
                // throw new RuntimeException("Inspection with RESOLVED status cannot be changed again");
            }


            case CLOSED -> {

                // CLOSED is final
                // throw new RuntimeException("Inspection with CLOSED status cannot be changed again");
            }


            default -> {

                throw new RuntimeException("Invalid current Inspection status: " + currentStatus);
            }
        }
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

    private void saveInspectionPhotos(List<MultipartFile> photos, InspectionEntity inspection) {

        if (photos == null || photos.isEmpty()) {
            return;
        }


        for (MultipartFile photo : photos) {

            if (photo == null || photo.isEmpty()) {
                continue;
            }


            try {

                // Save physical file
                String photoUrl = saveInspectionPhoto(photo, inspection.getId());


                // Save DB record
                InspectionPhotoEntity photoEntity = InspectionPhotoEntity.builder().inspection(inspection).photoUrl(photoUrl).build();


                inspectionPhotoRepository.save(photoEntity);

            } catch (IOException e) {

                log.error("Failed to save photo for Inspection: {}", inspection.getId(), e);


                throw new RuntimeException("Failed to save Inspection photo", e);
            }
        }
    }

    private String saveInspectionPhoto(MultipartFile photo, UUID inspectionId) throws IOException {

        String uploadDirectory = "uploads/inspection";


        // uploads/workOrder/{workOrderId}
        Path inspectionDirectory = Paths.get(uploadDirectory, inspectionId.toString());


        // Create directory
        Files.createDirectories(inspectionDirectory);


        // Original filename
        String originalFileName = photo.getOriginalFilename();


        String extension = "";


        if (originalFileName != null && originalFileName.contains(".")) {

            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }


        // Unique filename
        String fileName = UUID.randomUUID() + extension;


        // Final path
        Path filePath = inspectionDirectory.resolve(fileName);


        // Save file
        Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);


        // Return URL
        return "/uploads/inspection/" + inspectionId + "/" + fileName;
    }


    public List<GetAllInspectionGroupByStatusResponseDto> getAllInspectionGroupByStatus(String token) {

        log.info("Fetching all Inspections grouped by status started");

        // =========================
        // Token Validation
        // =========================
        if (token == null || token.isEmpty()) {
            log.error("Access token is empty or null");
            throw new RuntimeException("Invalid access token");
        }

        List<String> roles = jwtTokenProvider.getRolesFromJWT(token);

        log.info("Logged in user role: {}", roles);

      //  List<WorkOrderEntity> workOrders;
        List<InspectionEntity> inspections;


        // =========================
        // ADMIN -> All Inspections
        // OTHER ROLE -> Own Inspections
        // =========================

        boolean isAdmin = roles.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role));

        if (isAdmin) {

            log.info("ADMIN user detected. Fetching all inspections");

          //  workOrders = workOrderRepository.findAll();
            inspections = inspectionRepository.findAll();

        } else {

            String userId = jwtTokenProvider.getUserIdFromJWT(token);

            log.info("Fetching inspections for inspector: {}", userId);

            UUID userUUID;

            try {
                userUUID = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid user UUID: {}", userId);
                throw new RuntimeException("Invalid user id");
            }

            UserEntity user = userEntityRepository.findById(userUUID).orElseThrow(() -> new RuntimeException("User not found with id: " + userUUID));

          //  workOrders = workOrderRepository.findByInspectorId(user);

            inspections = inspectionRepository.findByInspectorUserId_Id(user.getId());

            log.info("Found {} inspections for inspector {}", inspections.size(), userUUID);
        }

        // =========================
        // Map Entity -> DTO
        // =========================
        List<GetAllInspectionGroupByStatusResponseDto.InspectorResponseDto> mappedInspections = inspections.stream().map(this::mapInspection).toList();

        // =========================
        // Group By Status
        // =========================
      //  Map<String, List<GetAllWorkOrderGroupByStatusResponseDto.WorkOrderComplaintResponseDto>> groupedWorkOrders = mappedWorkOrders.stream().filter(item -> item.getWorkOrder() != null && item.getWorkOrder().getStatus() != null).collect(Collectors.groupingBy(item -> item.getWorkOrder().getStatus(), LinkedHashMap::new, Collectors.toList()));

        Map<String, List<GetAllInspectionGroupByStatusResponseDto.InspectorResponseDto>> groupedInspection = mappedInspections.stream().filter(item -> item.getId() != null && item.getStatus() != null).collect(Collectors.groupingBy(item -> item.getStatus(), LinkedHashMap::new, Collectors.toList()));

        // =========================
        // ALL STATUS
        // =========================
        List<String> allStatuses = List.of("ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED");

        // =========================
        // Build Final Response
        // =========================
        List<GetAllInspectionGroupByStatusResponseDto> response = allStatuses.stream().map(status -> GetAllInspectionGroupByStatusResponseDto

                .builder().status(status).inspections(groupedInspection.getOrDefault(status,List.of())).build()).toList();

                //.status(status).workOrders(groupedWorkOrders.getOrDefault(status, List.of())).build()).toList();

        log.info("Inspections grouped successfully. Total groups: {}", response.size());

        return response;
    }


    private GetAllInspectionGroupByStatusResponseDto.InspectorResponseDto mapInspection(InspectionEntity inspection) {

        log.debug("Mapping inspection: {}", inspection.getId());

        // =========================
        // inspection Photos
        // =========================
        List<String> photos = inspection.getPhotos() == null ? List.of() : inspection.getPhotos().stream().map(InspectionPhotoEntity::getPhotoUrl).filter(Objects::nonNull).toList();

        log.debug("Inspection {} has {} photos", inspection.getId(), photos.size());

        // =========================
        // inspection DTO
        // =========================
        GetAllInspectionGroupByStatusResponseDto.InspectorResponseDto inspectionDto = GetAllInspectionGroupByStatusResponseDto.InspectorResponseDto.builder()
                .id(inspection.getId())
                .inspector(inspection.getInspectorUserId())
                .priority(String.valueOf(inspection.getPriority()))
                .status(String.valueOf(inspection.getStatus()))
                .workReport(inspection.getWorkReport())
                .dueDate(inspection.getDueDate())
                .asset(inspection.getAssetId() != null ? GetAllInspectionGroupByStatusResponseDto.AssetResponseDto.builder().id(inspection.getAssetId().getId()).name(inspection.getAssetId().getName()).status(inspection.getAssetId().getStatus()).condition(inspection.getAssetId().getCondition()).ward(inspection.getAssetId().getWard()).geometry(geometryService.fromJtsGeometry(inspection.getAssetId().getGeometry())).build() : null)
                .photos(photos).build();

//                .id(workOrder.getId())
//
//                .inspector(workOrder.getInspectorId() != null ? workOrder.getInspectorId() : null)
//
//                .priority(workOrder.getPriority() != null ? workOrder.getPriority().name() : null)
//
//                .status(workOrder.getStatus() != null ? workOrder.getStatus().name() : null)
//
//                .workReport(workOrder.getWorkReport())
//
//                .dueDate(workOrder.getDueDate())
//
//                .createdAt(workOrder.getCreatedAt())
//
//                .closedAt(workOrder.getClosedAt())
//
//                .photos(photos)
//
//                .build();

        // =========================
        // Complaint
        // =========================
//        ComplaintEntity complaint = workOrder.getComplaintId();
//
//        GetAllWorkOrderGroupByStatusResponseDto.ComplaintResponseDto complaintDto = null;
//
//        if (complaint != null) {
//
//            log.debug("Mapping complaint {} for work order {}", complaint.getId(), workOrder.getId());
//
//            complaintDto = mapComplaint(complaint);
//        }

        // =========================
        // Final Response
        // =========================
        return inspectionDto;
//                .workOrder(workOrderDto)
//
//                .complaint(complaintDto)
//
//                .build();
    }

}

