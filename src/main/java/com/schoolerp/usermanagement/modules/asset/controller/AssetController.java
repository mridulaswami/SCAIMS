package com.schoolerp.usermanagement.modules.asset.controller;


import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssignAssetRequest;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetResponseDto;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetCreateResponseDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.ChildAssetResponseDto;
import com.schoolerp.usermanagement.modules.asset.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/{assetId}/assign-inspector")
    public ResponseEntity<String> assignAssetToInspector(
            @PathVariable UUID assetId,
            @RequestBody AssignAssetRequest request){
        assetService.assignAssetToInspector(assetId,request);

        return ResponseEntity.ok("Asset assigned to inspector successfully");
    }

    @GetMapping("/inspector/{inspectorId}")
    public ResponseEntity<List<AssetResponseDto>> getAssetsByInspector(
            @PathVariable UUID inspectorId) {

        return ResponseEntity.ok(
                assetService.getAssetsByInspector(inspectorId)
        );
    }


}
