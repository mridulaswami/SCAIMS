package com.schoolerp.usermanagement.modules.audit.Repository;

import com.schoolerp.usermanagement.modules.audit.entity.InspectionStatusAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InspectionStatusAuditRepository extends JpaRepository<InspectionStatusAuditEntity, UUID> {
}
