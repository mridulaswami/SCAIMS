package com.schoolerp.usermanagement.modules.auth.repository;

import com.schoolerp.usermanagement.modules.auth.entity.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthEntityRepository extends JpaRepository<AuthEntity, UUID> {
    void deleteByUserId(UUID userId);
}
