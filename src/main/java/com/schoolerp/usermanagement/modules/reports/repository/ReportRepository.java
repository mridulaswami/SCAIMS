package com.schoolerp.usermanagement.modules.reports.repository;

import com.schoolerp.usermanagement.modules.reports.entity.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {
    Page<ReportEntity> findByCreatedByIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
