package com.schoolerp.usermanagement.modules.role.repository;

import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleEntityRepository extends JpaRepository <RoleEntity , UUID> {

    @Override
    Optional<RoleEntity> findById(UUID uuid);


}
