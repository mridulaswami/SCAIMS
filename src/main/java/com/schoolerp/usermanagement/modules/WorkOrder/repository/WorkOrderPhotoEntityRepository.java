package com.schoolerp.usermanagement.modules.WorkOrder.repository;

import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkOrderPhotoEntityRepository extends JpaRepository<WorkOrderPhotoEntity, UUID> {
}
