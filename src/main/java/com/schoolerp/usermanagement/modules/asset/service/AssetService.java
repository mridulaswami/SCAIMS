package com.schoolerp.usermanagement.modules.asset.service;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssignAssetRequest;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetResponseDto;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetCreateResponseDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.ChildAssetResponseDto;

import java.util.List;
import java.util.UUID;

public interface AssetService {

    public AssetCreateResponseDto createAsset(AssetrequestDto request);

    List<AssetResponseDto> getNearbyAssetParentsWithChildren(GeometryDto request);

    List<ChildAssetResponseDto> getChildAssets(UUID parentAssetId);

    void assignAssetToInspector(UUID asstId, AssignAssetRequest request);

    List<AssetResponseDto> getAssetsByInspector(UUID inspectorId);

}
