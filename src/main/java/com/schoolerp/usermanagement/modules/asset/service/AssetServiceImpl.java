package com.schoolerp.usermanagement.modules.asset.service;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.Geometry.GeometryService;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.entity.ParentAssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.asset.repository.ParentAssetRepository;
import com.schoolerp.usermanagement.modules.asset.repository.ParentAssetResponseDto;
import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetresponseDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.ChildAssetResponseDto;
import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import com.schoolerp.usermanagement.modules.assetCategory.repository.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetrepo;
    private final AssetCategoryRepository assetCategoryRepo;
    private final GeometryService geometryConverter;
    private static final double DEFAULT_RADIUS_METERS = 1000.0;
    private final ParentAssetRepository parentAssetRepository;

    @Override
    public AssetresponseDto createAsset(AssetrequestDto request) {
        try {

            AssetCategoryEntity assetCategoryId = assetCategoryRepo.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Asset category not found with id: " + request.getCategoryId()));

            Geometry geometry = geometryConverter.toJtsGeometry(request.getGeometry());

            AssetEntity asset = AssetEntity.builder().name(request.getName()).categoryId(assetCategoryId).geometry(geometry).status(request.getStatus()).condition(request.getCondition()).ward(request.getWard()).build();

            AssetEntity savedEntity = assetrepo.save(asset);

            return AssetresponseDto.builder().id(savedEntity.getId()).name(savedEntity.getName()).ward(savedEntity.getWard()).categoryId(assetCategoryId).build();

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentAssetResponseDto> getParentAssetByLocation(GeometryDto request) {

        log.info("Searching parent assets near location");

        if (request == null) {
            throw new IllegalArgumentException("Location request must not be null");
        }

        if (!"Point".equalsIgnoreCase(request.getType())) {
            throw new IllegalArgumentException("Location must be a Point geometry");
        }

        Geometry geometry = geometryConverter.toJtsGeometry(request);

        if (!(geometry instanceof Point point)) {
            throw new IllegalArgumentException("Invalid location geometry");
        }

        double longitude = point.getX();
        double latitude = point.getY();

        validateCoordinates(latitude, longitude);

        log.info("Searching assets | latitude={} | longitude={} | radius={} meters", latitude, longitude, DEFAULT_RADIUS_METERS);

        List<ParentAssetEntity> assets = parentAssetRepository.findAssetsWithinRadius(longitude, latitude, DEFAULT_RADIUS_METERS);

        log.info("Nearby parent assets found | count={}", assets.size());

        return assets.stream().map(this::mapToResponse).toList();
    }

    private ParentAssetResponseDto mapToResponse(ParentAssetEntity asset) {

        return ParentAssetResponseDto.builder().id(asset.getId()).name(asset.getName()).categoryId(asset.getCategory() != null ? asset.getCategory().getId() : null).geometry(geometryConverter.fromJtsGeometry(asset.getGeometry())).status(asset.getStatus()).condition(asset.getCondition()).ward(asset.getWard()).installedDate(asset.getInstalledDate()).lastInspectedDate(asset.getLastInspectedDate()).build();
    }

    private void validateCoordinates(double latitude, double longitude) {

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude. Latitude must be between -90 and 90");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude. Longitude must be between -180 and 180");
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<ChildAssetResponseDto> getChildAssets(UUID parentAssetId) {

        log.info("Searching child assets for parentAssetId={}", parentAssetId);

        if (parentAssetId == null) {
            throw new IllegalArgumentException("Parent Asset Id cannot be null");
        }

        ParentAssetEntity parentAsset = parentAssetRepository.findById(parentAssetId).orElseThrow(() -> new IllegalArgumentException("Parent Asset not found with id: " + parentAssetId));

        log.info("Parent asset found | id={} | name={}", parentAsset.getId(), parentAsset.getName());

        List<AssetEntity> assets = assetrepo.findByParentAssetId(parentAsset.getId());

        log.info("Child assets found | parentAssetId={} | count={}", parentAssetId, assets.size());

        return assets.stream().map(this::mapToAssetResponse).toList();
    }

    private ChildAssetResponseDto mapToAssetResponse(AssetEntity asset) {

        return ChildAssetResponseDto.builder().id(asset.getId()).name(asset.getName()).categoryId(asset.getCategoryId() != null ? asset.getCategoryId().getId() : null).parentAssetId(asset.getParentAsset() != null ? asset.getParentAsset().getId() : null).geometry(asset.getGeometry() != null ? geometryConverter.fromJtsGeometry(asset.getGeometry()) : null).status(asset.getStatus()).condition(asset.getCondition()).ward(asset.getWard()).installedDate(asset.getInstalledDate()).lastInspectionDate(asset.getLastInspectionDate()).build();
    }

}
