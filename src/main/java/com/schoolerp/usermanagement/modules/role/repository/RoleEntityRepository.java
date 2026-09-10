package com.schoolerp.usermanagement.modules.role.repository;

import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleEntityRepository extends JpaRepository <RoleEntity , Integer> {

    @Override
    Optional<RoleEntity> findById(Integer uuid);

    @Query(value = "SELECT role_name FROM roles WHERE id IN " +
            "(SELECT role_id FROM user_roles WHERE user_id = :id)",
            nativeQuery = true)
    List<String> findRoleNameByUserId(@Param("id") UUID id);


}
