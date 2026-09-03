package com.schoolerp.usermanagement.modules.WorkOrder.repository;

import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderEntityRepository extends JpaRepository<WorkOrderEntity, UUID> {

    @Query("""
            SELECT w
            FROM WorkOrderEntity w
            WHERE w.complaintId.id = :complaintId
            """)
    Optional<WorkOrderEntity> findByComplaintId(@Param("complaintId") UUID complaintId);

    long countByStatusNot(WorkOrderEntity.Status status);

    List<WorkOrderEntity> findByInspectorId(UserEntity user);
}
