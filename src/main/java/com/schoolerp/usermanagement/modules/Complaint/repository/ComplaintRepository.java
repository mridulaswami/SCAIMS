package com.schoolerp.usermanagement.modules.Complaint.repository;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintEntity, UUID> {

//    List<ComplaintEntity> findByCitizenId(Optional<UserEntity> citizenId);

    Page<ComplaintEntity> findByCitizenId(UserEntity citizen, Pageable pageable);

    long countByStatusNot(ComplaintEntity.Status status);


    @Query(value = """
        SELECT DISTINCT u.*
        FROM complaint u
        WHERE
            (:citizenId IS NULL OR u.citizen_id = :citizenId)
        AND
            (
                :search IS NULL
                OR :search = ''
                OR LOWER(u.pid) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.description) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        """,
            countQuery = """
        SELECT COUNT(DISTINCT u.id)
        FROM complaint u
        WHERE
            (:citizenId IS NULL OR u.citizen_id = :citizenId)
        AND
            (
                :search IS NULL
                OR :search = ''
                OR LOWER(u.pid) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.description) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        """,
            nativeQuery = true)
    Page<ComplaintEntity> findComplaintsFiltered(
            @Param("citizenId") UUID citizenId,
            @Param("search") String search,
            Pageable pageable);


    @Query(value = """
        SELECT DISTINCT u.*
        FROM complaint u
        WHERE
            (
                :search IS NULL
                OR :search = ''
                OR LOWER(u.pid) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.description) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        """,
            countQuery = """
        SELECT COUNT(DISTINCT u.id)
        FROM complaint u
        WHERE
            (
                :search IS NULL
                OR :search = ''
                OR LOWER(u.pid) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.description) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        """,
            nativeQuery = true)
    Page<ComplaintEntity> findComplaintsFilteredAdmin(@Param("search") String search, Pageable pageable);
}
