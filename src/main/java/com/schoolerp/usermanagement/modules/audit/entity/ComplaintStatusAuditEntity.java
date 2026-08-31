package com.schoolerp.usermanagement.modules.audit.entity;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaint_status_Audits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStatusAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false)
    private Status previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private Status currentStatus;

    @Column(name = "change_at", nullable = false)
    private LocalDateTime changeAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "changed_by", nullable = false)
    private UserEntity changedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id",nullable = false)
    private ComplaintEntity complaintId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = true)
    private LocalDateTime updatedAt;

    public enum Status {
        SUBMITTED, INPROGESS, REJECTED, COMPLETED
    }
}
