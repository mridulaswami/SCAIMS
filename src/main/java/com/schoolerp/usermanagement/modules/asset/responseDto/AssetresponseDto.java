package com.schoolerp.usermanagement.modules.asset.responseDto;


import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetresponseDto {

    private UUID id;
    private String name;
    private AssetCategoryEntity categoryId;
    private String ward;
}
