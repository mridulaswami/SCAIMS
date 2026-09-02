package com.schoolerp.usermanagement.modules.assetCategory.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.assetCategory.responseDto.AssetCategoryResponseDto;
import com.schoolerp.usermanagement.modules.assetCategory.service.AssetCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assetCategory")
@RequiredArgsConstructor
@Slf4j
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssetCategoryResponseDto>>> getAssetCategory(){

        List<AssetCategoryResponseDto> assetCategory = assetCategoryService.getAllAssetCategory();

        return ResponseEntity.ok(ApiResponse.of(true,"Got Asset Category Successfully" , assetCategory));

    }

}
