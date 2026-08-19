package com.schoolerp.usermanagement.modules.auth.repository;

import com.schoolerp.usermanagement.modules.auth.entity.AuthEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthEntityRepository extends JpaRepository<AuthEntity, UUID> {
    void deleteByUserId(UserEntity userId);

    Optional<AuthEntity> findByUserId(UserEntity userId);
}
