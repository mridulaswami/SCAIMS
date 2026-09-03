package com.schoolerp.usermanagement.modules.auth.repository;

import com.schoolerp.usermanagement.modules.auth.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpEntityRepository extends JpaRepository<OtpEntity, UUID> {

    Optional<OtpEntity> findTopByEmailOrderByCreatedAtDesc(String email);
}
