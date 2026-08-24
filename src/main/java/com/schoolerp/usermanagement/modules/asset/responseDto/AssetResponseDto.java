package com.schoolerp.usermanagement.modules.asset.responseDto;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
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

    private UUID categoryId;

    private GeometryDto geometry;

    private UUID parentAssetId;

    private String status;

    private String condition;

    private String ward;

    private LocalDateTime installedDate;

    private LocalDateTime lastInspectedDate;

    private List<AssetResponseDto> children;
}