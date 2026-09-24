package com.schoolerp.usermanagement.modules.asset.controller;


import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.common.response.PaginationResponse;
import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetResponseDto;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetCreateResponseDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.ChildAssetResponseDto;
import com.schoolerp.usermanagement.modules.asset.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController {

    private final AssetService assetService;
    private final AssetRepository assetRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<AssetCreateResponseDto>> createAsset(@Valid @RequestBody AssetrequestDto request) {
        try {

            AssetCreateResponseDto response = assetService.createAsset(request);

            return ResponseEntity.ok(ApiResponse.of(true, "Asset created successfully", response));

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    @PostMapping("/nearby")
    public ResponseEntity<ApiResponse<List<AssetResponseDto>>> getNearbyAssetParentsWithChildren(@RequestBody GeometryDto request) {

        try {

            List<AssetResponseDto> assets = assetService.getNearbyAssetParentsWithChildren(request);

            return ResponseEntity.ok(ApiResponse.of(true, "Nearby assets fetched successfully", assets));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(ApiResponse.of(false, e.getMessage(), null));

        } catch (Exception e) {

            log.error("Error while fetching nearby assets", e);

            return ResponseEntity.internalServerError().body(ApiResponse.of(false, "Failed to fetch nearby assets", null));
        }
    }


    @GetMapping("/parent/{parentAssetId}/children")
    public ResponseEntity<ApiResponse<List<ChildAssetResponseDto>>> getChildAssets(@PathVariable UUID parentAssetId) {

        try {

            List<ChildAssetResponseDto> assets = assetService.getChildAssets(parentAssetId);

            return ResponseEntity.ok(ApiResponse.of(true, "Child assets fetched successfully", assets));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(ApiResponse.of(false, e.getMessage(), null));

        } catch (Exception e) {

            log.error("Error while fetching child assets | parentAssetId={}", parentAssetId, e);

            return ResponseEntity.internalServerError().body(ApiResponse.of(false, "Failed to fetch child assets", null));
        }
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<List<AssetResponseDto>>> getAssets(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID categoryId, @RequestParam(required = false) String search){

        PaginationResponse<List<AssetResponseDto>> assets = assetService.getAssets(page,size,categoryId,search);

        return ResponseEntity.ok(assets);
    }

    @GetMapping("/assetCategoryId/{id}")
    public ResponseEntity<ApiResponse<Page<AssetResponseDto>>> getAssetByCategory(@PathVariable UUID id, @PageableDefault (size =5) Pageable pageable){

        Page<AssetResponseDto> assets = assetService.getAssetsByCategory(id , pageable);

        return ResponseEntity.ok(ApiResponse.of(true,"Got assets successfully for given category",assets));
    }

    @GetMapping("/name/search")
    public ResponseEntity<PaginationResponse<List<AssetResponseDto>>> getAssetByName(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam String name){

        PaginationResponse<List<AssetResponseDto>> assetList = assetService.getAssetsByName(page, size, name);

        return ResponseEntity.ok(assetList);

    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<AssetResponseDto>> getAssetsById(@PathVariable UUID id){

        AssetResponseDto asset = assetService.getAssetsbyid(id);

        return ResponseEntity.ok(ApiResponse.of(true,"Got asset successfully for given id",asset));


    }


}
