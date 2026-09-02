package com.schoolerp.usermanagement.modules.assetCategory.service;

import com.schoolerp.usermanagement.modules.asset.responseDto.AssetResponseDto;
import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import com.schoolerp.usermanagement.modules.assetCategory.repository.AssetCategoryRepository;
import com.schoolerp.usermanagement.modules.assetCategory.responseDto.AssetCategoryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetCategoryServiceImpl implements AssetCategoryService{

    private final AssetCategoryRepository assetCategoryRepository;

    @Override
    public List<AssetCategoryResponseDto> getAllAssetCategory() {

        List<AssetCategoryEntity> assetCategoryList = assetCategoryRepository.findAll();


        return assetCategoryList.stream().map(assetCategory -> {

            return AssetCategoryResponseDto.builder().name(assetCategory.getName()).build();
        }).collect(Collectors.toList());
    }
}
