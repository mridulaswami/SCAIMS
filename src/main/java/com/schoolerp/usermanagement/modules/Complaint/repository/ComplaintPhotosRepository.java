package com.schoolerp.usermanagement.modules.Complaint.repository;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintPhotosEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Repository
public interface ComplaintPhotosRepository extends JpaRepository<ComplaintPhotosEntity, UUID> {

    List<ComplaintPhotosEntity> findByComplaintId(UUID complaintId);
}
