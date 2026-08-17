package com.schoolerp.usermanagement.modules.asset.requestDto;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetrequestDto {

    private String name;
    private UUID categoryId;
    private GeometryDto geometry;
    private String status;
    private String condition;
    private String ward;


}
