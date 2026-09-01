package com.schoolerp.usermanagement.modules.WorkOrder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Table(name = "work_order")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "complaint_id")
    private ComplaintEntity complaintId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity inspectorId;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "work_report", nullable = true)
    private String workReport;

    @CreationTimestamp
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

//    @CreationTimestamp
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "workOrderId", fetch = FetchType.EAGER)
    private List<WorkOrderPhotoEntity> photos;

    public enum Status {
        ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
    }

    public enum Priority {
        LOW, HIGH, MEDIUM
    }

}
