package com.schoolerp.usermanagement.modules.assetCategory.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Table(name="asset_category")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name",unique = true)
    private String name;

    @Column(name="icon_key")
    private String iconKey;

    @Column(name="default_layer_color")
    private String defaultLayerColor;
}
