package com.schoolerp.usermanagement.modules.asset.service;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetResponseDto;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetCreateResponseDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.ChildAssetResponseDto;
import org.springframework.data.domain.Page;


import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface AssetService {

    public AssetCreateResponseDto createAsset(AssetrequestDto request);

    List<AssetResponseDto> getNearbyAssetParentsWithChildren(GeometryDto request);

    List<ChildAssetResponseDto> getChildAssets(UUID parentAssetId);

    Page<AssetResponseDto> getAllAssets(Pageable pageable);

    Page<AssetResponseDto> getAssetsByCategory(UUID id , Pageable pageable);

     AssetResponseDto getAssetsbyid(UUID id);

     List<AssetResponseDto> getAssetsByName(String name);

}
