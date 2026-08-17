package com.schoolerp.usermanagement.modules.WorkOrder.entity;

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
import java.util.UUID;

@Table(name="work_order")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private AssetEntity assetId;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private InspectionEntity assignedTo;

    @Column(name="priority")
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private Status status;

    @CreationTimestamp
    @Column(name="due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    @Column(name="created_at")
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name="closed_at")
    private LocalDateTime closedAt;

    public enum Status {
        OPEN, ASSIGNED, IN_PROGRESS, RESOLVED , CLOSED
    }

}
