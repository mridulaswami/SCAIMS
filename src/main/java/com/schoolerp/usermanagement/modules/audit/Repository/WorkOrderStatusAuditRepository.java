package com.schoolerp.usermanagement.modules.audit.Repository;

import com.schoolerp.usermanagement.modules.audit.entity.WorkOrderStatusAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkOrderStatusAuditRepository extends JpaRepository<WorkOrderStatusAuditEntity, UUID> {
}
