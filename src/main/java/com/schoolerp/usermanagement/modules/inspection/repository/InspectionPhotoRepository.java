package com.schoolerp.usermanagement.modules.inspection.repository;

import com.schoolerp.usermanagement.modules.inspection.entity.InspectionPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionPhotoRepository extends JpaRepository<InspectionPhotoEntity, UUID> {

    List<InspectionPhotoEntity> findByInspection_Id(UUID inspectionId);
}