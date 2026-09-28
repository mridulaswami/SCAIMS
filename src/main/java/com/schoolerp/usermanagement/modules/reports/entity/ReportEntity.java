package com.schoolerp.usermanagement.modules.reports.entity;

import com.schoolerp.usermanagement.modules.reports.enums.ReportFormat;
import com.schoolerp.usermanagement.modules.reports.enums.ReportStatus;
import com.schoolerp.usermanagement.modules.reports.enums.ReportType;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", indexes = {@Index(name = "idx_reports_created_by", columnList = "created_by"), @Index(name = "idx_reports_report_type", columnList = "report_type"), @Index(name = "idx_reports_status", columnList = "status"), @Index(name = "idx_reports_created_at", columnList = "created_at")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "report_type", nullable = false, columnDefinition = "report_type_enum")
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "report_status_enum")
    private ReportStatus status;

    @Column(nullable = false, length = 255)
    private String reportName;

    @Column(name = "url", length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "report_format", nullable = false, columnDefinition = "report_format_enum")
    private ReportFormat reportFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @CreationTimestamp
    @Column(name = "report_initiated_at", nullable = false, updatable = false)
    private LocalDateTime reportInitiatedAt;

    @Column(name = "report_completed_at")
    private LocalDateTime reportCompletedAt;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
