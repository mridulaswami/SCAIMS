package com.schoolerp.usermanagement.modules.asset.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name="parent_assets")

public class ParentAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Column(name = "name" , nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private AssetCategoryEntity category;

    @Column(name = "geometry", columnDefinition = "geometry(Polygon,4326)", nullable = false)
    private Polygon geometry;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "condition", length = 100)
    private String condition;

    @Column(name = "ward", length = 100)
    private String ward;

    @Column(name = "installed_date")
    private LocalDateTime installedDate;

    @Column(name = "last_inspected_date")
    private LocalDateTime lastInspectedDate;
}



