package com.schoolerp.usermanagement.modules.Complaint.repository;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintEntity, UUID> {

    List<ComplaintEntity> findByCitizenId(Optional<UserEntity> citizenId);

    long countByStatusNot(ComplaintEntity.Status status);
}
