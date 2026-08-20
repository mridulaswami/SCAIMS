package com.schoolerp.usermanagement.modules.asset.service;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.entity.ParentAssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.ParentAssetResponseDto;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetresponseDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.ChildAssetResponseDto;

import java.util.List;
import java.util.UUID;

public interface AssetService {

    public AssetresponseDto createAsset(AssetrequestDto request);

    List<ParentAssetResponseDto> getParentAssetByLocation(GeometryDto request);

    List<ChildAssetResponseDto> getChildAssets(UUID parentAssetId);

}
