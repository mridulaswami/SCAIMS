package com.schoolerp.usermanagement.modules.asset.service;

import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetresponseDto;
import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import com.schoolerp.usermanagement.modules.assetCategory.repository.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService{

    private final AssetRepository assetrepo;
    private final AssetCategoryRepository assetCategoryRepo;

    @Override
    public AssetresponseDto createAsset(AssetrequestDto request) {
        try{

            AssetCategoryEntity assetCategoryId =
                    assetCategoryRepo.findById(request.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("Asset category not found with id: " + request.getCategoryId()));
            AssetEntity asset = AssetEntity.builder().name(request.getName()).categoryId(assetCategoryId)
                  //  .geometry(request.getGeometry())
                    .status(request.getStatus()).condition(request.getCondition()).ward(request.getWard()).build();

            AssetEntity savedEntity = assetrepo.save(asset);

            return AssetresponseDto.builder().id(savedEntity.getId()).name(savedEntity.getName()).ward(savedEntity.getWard()).categoryId(assetCategoryId).build();

        } catch (Exception e) {
            throw e;
        }
    }
}
