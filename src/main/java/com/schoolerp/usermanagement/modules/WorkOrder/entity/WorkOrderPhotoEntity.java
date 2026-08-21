package com.schoolerp.usermanagement.modules.WorkOrder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_order_photo")
public class WorkOrderPhotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "work_order_id")
    private WorkOrderEntity workOrderId;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updatedAt", nullable = false, updatable = true)
    private LocalDateTime updatedAt;

}
