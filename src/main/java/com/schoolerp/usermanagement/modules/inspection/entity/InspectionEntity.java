package com.schoolerp.usermanagement.modules.inspection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table(name="inspection")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InspectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private AssetEntity assetId;

    @ManyToOne
    @JoinColumn(name= "inspector_user_id")
    private UserEntity inspectorUserId;

    @Column(name= "notes")
    private String notes;

    @CreationTimestamp
    @Column(name= "inspected_at")
    private LocalDateTime inspectedAt;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private InspectionEntity.Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InspectionEntity.Status status;

    @Column(name = "work_report", nullable = true)
    private String workReport;

    @Column(name = "rejection_reason", nullable = true)
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;


    @CreationTimestamp
    @Column(name = "due_date")
    private LocalDateTime dueDate;

//    @Column(name = "geo_tag" , columnDefinition = "geometry(Point,4326)" , nullable = false)
//    private Point geoTag;

    @OneToMany(mappedBy = "inspection", fetch = FetchType.EAGER)
    private List<InspectionPhotoEntity> photos;


    public enum Status {
        ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
    }

    public enum Priority {
        LOW, HIGH, MEDIUM
    }
}