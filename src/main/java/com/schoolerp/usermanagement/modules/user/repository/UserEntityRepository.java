package com.schoolerp.usermanagement.modules.user.repository;

import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUserName(String userName);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    Optional<UserEntity> findById(UUID userId);

    boolean existsByPhone(String phone);
}
