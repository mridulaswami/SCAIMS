package com.schoolerp.usermanagement.modules.inspection.entity;

import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
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

    @OneToOne
    @JoinColumn(name="inspector_user_id")
    private UserEntity inspectorUserId;

    @Column(name="condition_rating")
    private String conditionRating;

    @Column(name="notes")
    private String notes;

    @Column(name="photo_url")
    private String photoUrl;

    @CreationTimestamp
    @Column(name="inspected_at")
    private LocalDateTime inspectedAt;

    @Column(name="geo_tag" , columnDefinition = "geometry(Point,4326)")
    private Geometry geoTag;

}
