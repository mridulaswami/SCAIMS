package com.schoolerp.usermanagement.modules.asset.controller;


import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetresponseDto;
import com.schoolerp.usermanagement.modules.asset.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssetresponseDto>> createAsset(@Valid @RequestBody AssetrequestDto request){
        try{

            AssetresponseDto response = assetService.createAsset(request);

            return ResponseEntity.ok(ApiResponse.of(true, "Asset created successfully", response));

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

}
