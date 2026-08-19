package com.schoolerp.usermanagement.modules.Complaint.repository;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintEntity, UUID> {
}
