package com.schoolerp.usermanagement.modules.asset.responseDto;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildAssetResponseDto {

    private UUID id;

    private String name;

    private UUID categoryId;

    private UUID parentAssetId;

    private GeometryDto geometry;

    private String status;

    private String condition;

    private String ward;

    private LocalDateTime installedDate;

    private LocalDateTime lastInspectionDate;
}