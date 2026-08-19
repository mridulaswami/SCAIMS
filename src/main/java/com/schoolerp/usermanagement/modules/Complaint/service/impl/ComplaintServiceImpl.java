package com.schoolerp.usermanagement.modules.Complaint.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
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
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ComplaintResponseDto createComplaint(CreateComplaintRequestDto request) {

        log.info("Creating complaint | citizenId={} | title={} | assetId={}", request.getCitizenId(), request.getTitle(), request.getAsset());

        // Step 1: Find citizen
        log.debug("Fetching citizen | citizenId={}", request.getCitizenId());

        UserEntity citizen = userEntityRepository.findById(request.getCitizenId()).orElseThrow(() -> {
            log.warn("Citizen not found | citizenId={}", request.getCitizenId());

            return new RuntimeException("Citizen not found with id: " + request.getCitizenId());
        });

        // Step 2: Find asset
        log.debug("Fetching asset | assetId={}", request.getAsset());

        AssetEntity asset = assetRepository.findById(request.getAsset()).orElseThrow(() -> {
            log.warn("Asset not found | assetId={}", request.getAsset());

            return new RuntimeException("Asset not found with id: " + request.getAsset());
        });

        // Step 3: Convert geometry
        log.debug("Converting complaint geometry | citizenId={} | assetId={}", request.getCitizenId(), request.getAsset());

        GeometryDto geometryDto;

        try {
            geometryDto = objectMapper.readValue(request.getGeometry(), GeometryDto.class);
        } catch (Exception e) {

            log.error("Invalid complaint geometry | geometry={}", request.getGeometry(), e);

            throw new IllegalArgumentException("Invalid geometry format");
        }

        Geometry geometry = geometryConverter.toJtsGeometry(geometryDto);

        if (geometry == null) {

            log.warn("Complaint geometry conversion returned null | citizenId={}", request.getCitizenId());

            throw new IllegalArgumentException("Complaint location is required");
        }

        // Step 4: Create complaint
        ComplaintEntity complaint = ComplaintEntity.builder().citizenId(citizen).title(request.getTitle()).asset(asset).description(request.getDescription()).location(geometry).status(ComplaintEntity.Status.SUBMITTED).build();

        log.debug("Complaint entity prepared | citizenId={} | assetId={} | geometryType={} | status={}", request.getCitizenId(), request.getAsset(), geometry.getGeometryType(), complaint.getStatus());

        // Step 5: Save complaint
        ComplaintEntity savedComplaint = complaintRepository.save(complaint);

        log.info("Complaint created successfully | complaintId={} | citizenId={} | assetId={} | status={}", savedComplaint.getId(), request.getCitizenId(), request.getAsset(), savedComplaint.getStatus());

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
    @Transactional
    public List<GetAllComplaintsResponseDto> getAllComplaints() {

        log.info("Started getting all complaints");

        List<ComplaintEntity> complaints = complaintRepository.findAll();

        log.info("Complaints fetched successfully | count={}", complaints.size());

        return complaints.stream().map(complaint -> {

            List<String> photos = complaintPhotosRepository.findByComplaintId(complaint.getId()).stream().map(ComplaintPhotosEntity::getPhotoUrl).toList();

            return GetAllComplaintsResponseDto.builder().id(complaint.getId()).citizenId(complaint.getCitizenId().getId()).assetId(complaint.getAsset().getId()).title(complaint.getTitle()).description(complaint.getDescription()).status(complaint.getStatus().name()).location(geometryConverter.fromJtsGeometry(complaint.getLocation())).photos(photos).build();
        }).toList();
    }
}