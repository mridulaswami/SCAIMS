package com.schoolerp.usermanagement.modules.user.repository;

import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Optional<UserEntity> findByUserNameOrEmailOrPhone(String userName, String email, String phone);

    @Query("SELECT u FROM UserEntity u WHERE " + "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " + "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " + "u.phone LIKE CONCAT('%', :search, '%')")
    Page<UserEntity> searchByNameOrEmailOrPhone(@Param("search") String search, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT u
            FROM UserEntity u
            LEFT JOIN UserRoleEntity ur ON ur.user = u
            LEFT JOIN ur.role r
            WHERE
                (:roleId IS NULL OR r.id = :roleId)
            AND
                (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))
                ) order by u.createdAt desc
            """, countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM UserEntity u
            LEFT JOIN UserRoleEntity ur ON ur.user = u
            LEFT JOIN ur.role r
            WHERE
                (:roleId IS NULL OR r.id = :roleId)
            AND
                (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            """)
    Page<UserEntity> findUsersFiltered(@Param("roleId") Integer roleId, @Param("search") String search, Pageable pageable);

}
