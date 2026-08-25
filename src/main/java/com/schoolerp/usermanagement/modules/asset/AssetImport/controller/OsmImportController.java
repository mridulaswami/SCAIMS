package com.schoolerp.usermanagement.modules.asset.AssetImport.controller;


import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.OsmImportRequestDto;
import com.schoolerp.usermanagement.modules.asset.AssetImport.service.OsmImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assets/import")
@RequiredArgsConstructor
@Slf4j
public class OsmImportController {

    private final OsmImportService osmImportService;

    @PostMapping("/osm")
    public ResponseEntity<?> importAssets(@RequestBody OsmImportRequestDto request) {
        osmImportService.importAssets(request);
        return ResponseEntity.ok("Assets imported successfully");
    }


}
