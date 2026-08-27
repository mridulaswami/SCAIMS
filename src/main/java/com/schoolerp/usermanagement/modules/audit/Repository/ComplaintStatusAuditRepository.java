package com.schoolerp.usermanagement.modules.audit.Repository;

import com.schoolerp.usermanagement.modules.audit.entity.ComplaintStatusAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplaintStatusAuditRepository extends JpaRepository<ComplaintStatusAuditEntity, UUID> {
}
