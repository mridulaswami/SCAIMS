package com.schoolerp.usermanagement.modules.asset.AssetImport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverpassElementDto {

    String type;
    long id;
    Double lat;
    Double lon;

    List<GeometryPointDto> geometry;

    Map<String, String> tags;

}



