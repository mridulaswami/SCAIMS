package com.schoolerp.usermanagement.modules.Complaint.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.schoolerp.usermanagement.common.response.PaginationResponse;
import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintPhotosEntity;
import com.schoolerp.usermanagement.modules.Complaint.repository.ComplaintPhotosRepository;
import com.schoolerp.usermanagement.modules.Complaint.repository.ComplaintRepository;
import com.schoolerp.usermanagement.modules.Complaint.requestDto.CreateComplaintRequestDto;
import com.schoolerp.usermanagement.modules.Complaint.responseDto.ComplaintResponseDto;
import com.schoolerp.usermanagement.modules.Complaint.responseDto.GetAllComplaintsResponseDto;
import com.schoolerp.usermanagement.modules.Complaint.service.ComplaintService;
import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.Geometry.GeometryService;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderPhotoEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.responseDto.WorkOrderResponseDto;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserEntityRepository userEntityRepository;
    private final ComplaintPhotosRepository complaintPhotosRepository;
    private final AssetRepository assetRepository;
    private final GeometryService geometryConverter;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ComplaintResponseDto createComplaint(CreateComplaintRequestDto request, UUID citizenId) {

        log.info("Creating complaint | citizenId={} | title={} | assetId={}", citizenId, request.getTitle(), request.getAsset());

        // Step 1: Find citizen
        log.debug("Fetching citizen | citizenId={}", citizenId);

        UserEntity citizen = userEntityRepository.findById(citizenId).orElseThrow(() -> {
            log.warn("Citizen not found | citizenId={}", citizenId);

            return new RuntimeException("Citizen not found with id: " + citizenId);
        });

        // Step 2: Find asset
        log.debug("Fetching asset | assetId={}", request.getAsset());

        AssetEntity asset = assetRepository.findById(request.getAsset()).orElseThrow(() -> {
            log.warn("Asset not found | assetId={}", request.getAsset());

            return new RuntimeException("Asset not found with id: " + request.getAsset());
        });

        // Step 3: Convert location
        log.debug("Converting complaint location | citizenId={} | assetId={}", citizenId, request.getAsset());

        GeometryDto geometryDto;

        try {

            JsonNode locationNode = objectMapper.readTree(request.getLocation());

            JsonNode coordinatesNode = locationNode.get("coordinates");

            // Validate coordinates
            if (coordinatesNode == null || !coordinatesNode.isArray()) {

                log.warn("Invalid complaint coordinates | location={}", request.getLocation());

                throw new IllegalArgumentException("Location coordinates are required");
            }

            // Point must contain exactly longitude and latitude
            if (coordinatesNode.size() != 2) {

                log.warn("Invalid Point coordinates | coordinates={}", coordinatesNode);

                throw new IllegalArgumentException("Point must contain longitude and latitude");
            }

            double longitude = coordinatesNode.get(0).asDouble();
            double latitude = coordinatesNode.get(1).asDouble();

            // Validate coordinate range
            if (longitude < -180 || longitude > 180) {

                throw new IllegalArgumentException("Invalid longitude. Must be between -180 and 180");
            }

            if (latitude < -90 || latitude > 90) {

                throw new IllegalArgumentException("Invalid latitude. Must be between -90 and 90");
            }

            // Backend hardcodes Point
            geometryDto = new GeometryDto();
            geometryDto.setType("Point");

            List<Double> coordinates = new ArrayList<>();
            coordinates.add(longitude);
            coordinates.add(latitude);

            geometryDto.setCoordinates(coordinates);

            log.debug("Complaint location parsed successfully | longitude={} | latitude={}", longitude, latitude);

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            log.error("Invalid complaint location | location={}", request.getLocation(), e);

            throw new IllegalArgumentException("Invalid location format. Expected: {\"coordinates\":[longitude,latitude]}");
        }

        Geometry geometry = geometryConverter.toJtsGeometry(geometryDto);

        if (geometry == null) {

            log.warn("Complaint geometry conversion returned null | citizenId={} | assetId={}", citizenId, request.getAsset());

            throw new IllegalArgumentException("Complaint location is required");
        }

        // Step 4: Create complaint
        ComplaintEntity complaint = ComplaintEntity.builder().citizenId(citizen).title(request.getTitle()).asset(asset).description(request.getDescription()).location(geometry).status(ComplaintEntity.Status.SUBMITTED).build();

        log.debug("Complaint entity prepared | citizenId={} | assetId={} | geometryType={} | status={}", citizenId, request.getAsset(), geometry.getGeometryType(), complaint.getStatus());

        // Step 5: Save complaint
        ComplaintEntity savedComplaint = complaintRepository.save(complaint);

        log.info("Complaint created successfully | complaintId={} | citizenId={} | assetId={} | status={}", savedComplaint.getId(), citizenId, request.getAsset(), savedComplaint.getStatus());

        // Step 6: Save complaint photos
        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {

            log.info("Complaint photos received | complaintId={} | photoCount={}", savedComplaint.getId(), request.getPhotos().size());

            for (MultipartFile photo : request.getPhotos()) {

                if (photo == null || photo.isEmpty()) {

                    log.warn("Skipping empty complaint photo | complaintId={}", savedComplaint.getId());

                    continue;
                }

                try {

                    String photoUrl = saveComplaintPhoto(photo, savedComplaint.getId());

                    ComplaintPhotosEntity complaintPhoto = ComplaintPhotosEntity.builder().complaint(savedComplaint).photoUrl(photoUrl).build();

                    complaintPhotosRepository.save(complaintPhoto);

                    log.info("Complaint photo saved | complaintId={} | fileName={} | photoUrl={}", savedComplaint.getId(), photo.getOriginalFilename(), photoUrl);

                } catch (IOException e) {

                    log.error("Failed to save complaint photo | complaintId={} | fileName={}", savedComplaint.getId(), photo.getOriginalFilename(), e);

                    throw new RuntimeException("Failed to save complaint photo", e);
                }
            }

        } else {

            log.debug("No photos received | complaintId={}", savedComplaint.getId());
        }

        // Step 7: Create response
        return ComplaintResponseDto.builder().title(savedComplaint.getTitle()).description(savedComplaint.getDescription()).build();
    }

    private String saveComplaintPhoto(MultipartFile photo, UUID complaintId) throws IOException {

        String uploadDirectory = "uploads/complaints";

        Path complaintDirectory = Paths.get(uploadDirectory, complaintId.toString());

        Files.createDirectories(complaintDirectory);

        String originalFileName = photo.getOriginalFilename();

        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {

            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID() + extension;

        Path filePath = complaintDirectory.resolve(fileName);

        Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/complaints/" + complaintId + "/" + fileName;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PaginationResponse<List<GetAllComplaintsResponseDto>> getAllComplaints(String token, int page, int size) {

        log.info("Started getting all complaints | page={} | size={}", page, size);

        String role = jwtTokenProvider.getRoleFromJWT(token);
        String userId = jwtTokenProvider.getUserIdFromJWT(token);

        log.info("Fetched role={} | userId={}", role, userId);

        if (role == null || role.isBlank()) {
            throw new RuntimeException("User role not found in token");
        }

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("User ID not found in token");
        }

        UUID userUuid = UUID.fromString(userId);

        Optional<UserEntity> userOptional = userEntityRepository.findById(userUuid);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ComplaintEntity> complaintPage;

        if ("ADMIN".equalsIgnoreCase(role)) {

            log.debug("Admin user | Fetching all complaints");

            complaintPage = complaintRepository.findAll(pageable);

        } else {

            log.debug("Citizen user | Fetching complaints | userId={}", userUuid);

            complaintPage = complaintRepository.findByCitizenId(userOptional.get(), pageable);
        }

        log.info("Complaints fetched successfully | page={} | size={} | totalElements={}", page, size, complaintPage.getTotalElements());

        List<GetAllComplaintsResponseDto> response = complaintPage.getContent().stream().map(complaint -> {

            List<String> complaintPhotos = complaintPhotosRepository.findByComplaintId(complaint.getId()).stream().map(ComplaintPhotosEntity::getPhotoUrl).toList();

            WorkOrderResponseDto workOrderDto = null;

            if (complaint.getWorkOrder() != null) {

                WorkOrderEntity workOrder = complaint.getWorkOrder();

                List<String> workOrderPhotos = workOrder.getPhotos() != null ? workOrder.getPhotos().stream().map(WorkOrderPhotoEntity::getPhotoUrl).toList() : List.of();

                workOrderDto = WorkOrderResponseDto.builder().id(workOrder.getId()).complaintId(workOrder.getComplaintId() != null ? workOrder.getComplaintId().getId() : null).inspectorId(workOrder.getInspectorId() != null ? workOrder.getInspectorId().getId() : null).priority(workOrder.getPriority() != null ? workOrder.getPriority().name() : null).status(workOrder.getStatus() != null ? workOrder.getStatus().name() : null).inspector(workOrder.getInspectorId()).workReport(workOrder.getWorkReport()).dueDate(workOrder.getDueDate()).createdAt(workOrder.getCreatedAt()).closedAt(workOrder.getClosedAt()).photos(workOrderPhotos).build();
            }

            return GetAllComplaintsResponseDto.builder().id(complaint.getId()).citizenId(complaint.getCitizenId() != null ? complaint.getCitizenId().getId() : null).assetId(complaint.getAsset() != null ? complaint.getAsset().getId() : null).title(complaint.getTitle()).description(complaint.getDescription()).status(complaint.getStatus() != null ? complaint.getStatus().name() : null).location(complaint.getLocation() != null ? geometryConverter.fromJtsGeometry(complaint.getLocation()) : null).photos(complaintPhotos).workOrder(workOrderDto).createdAt(complaint.getCreatedAt()).updatedAt(complaint.getUpdatedAt()).build();
        }).toList();

        return new PaginationResponse<>(response, complaintPage.getTotalElements(), complaintPage.getNumber(), complaintPage.getSize());
    }
}