package com.schoolerp.usermanagement.modules.asset.AssetImport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsmImportRequestDto {

    public double south;
    public double west;
    public double north;
    public double east;

}
