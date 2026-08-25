package com.schoolerp.usermanagement.modules.asset.entity;

import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Geometry;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assets")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private AssetCategoryEntity categoryId;

    @Column(name = "geometry", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geometry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_asset_id")
    private AssetEntity parentAsset;

    @Column(name = "status")
    private String status;

    @Column(name = "condition")
    private String condition;

    @Column(name = "ward")
    private String ward;

    @CreationTimestamp
    @Column(name = "installed_date")
    private LocalDateTime installedDate;

    @CreationTimestamp
    @Column(name = "last_inspected_date")
    private LocalDateTime lastInspectionDate;

    private String source_type;

    private long source_id;
}