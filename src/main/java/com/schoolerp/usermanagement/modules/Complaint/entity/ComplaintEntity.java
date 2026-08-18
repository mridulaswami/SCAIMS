package com.schoolerp.usermanagement.modules.Complaint.entity;

import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
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

import java.time.LocalTime;
import java.util.UUID;

@Table(name="complaint")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "citizen_id")
    private UserEntity citizenId;


    @Column(name="category")
    private String category;

    @Column(name="description")
    private String description;

    @Column(name="location" , columnDefinition = "geometry(Point,4326)")
    private Geometry location;

    @Column(name="photo_url")
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private Status status;

    @OneToOne
    @JoinColumn(name = "linked_work_order_id")
    private WorkOrderEntity linkedWorkOrderId;


    public enum Status {
        SUBMITTED, TRIAGED, CONVERTED, REJECTED
    }

}
