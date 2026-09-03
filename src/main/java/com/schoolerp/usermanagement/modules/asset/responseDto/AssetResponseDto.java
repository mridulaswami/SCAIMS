package com.schoolerp.usermanagement.modules.asset.responseDto;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetResponseDto {

    private UUID id;

    private String name;

    private String categoryName;

    private UUID categoryId;

    private AssetCategoryEntity assetCategory;

    private GeometryDto geometry;

    private UUID parentAssetId;

    private String status;

    private String condition;

    private String ward;

    private LocalDateTime installedDate;

    private LocalDateTime lastInspectedDate;

    private List<AssetResponseDto> children;
}