package com.schoolerp.usermanagement.modules.asset.AssetImport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverPassResponseDto {

 List<OverpassElementDto> elements;
}
