package com.schoolerp.usermanagement.modules.inspection.service;

import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
import com.schoolerp.usermanagement.modules.inspection.repository.InspectionRepository;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final AssetRepository assetRepository;
    private final UserEntityRepository userEntityRepository;

    @Transactional
    public InspectionEntity createInspection(
            UUID assetId,
            UUID inspectorUserId,
            String notes,
            Double latitude,
            Double longitude
    ){
        if (latitude == null || longitude == null){
            throw new IllegalArgumentException("Latitude and longitude are required");
        }
//        if (geoTag == null){
//            throw new IllegalArgumentException("Geotag required");
//        }
        AssetEntity asset = assetRepository.findById(assetId)
                .orElseThrow(() ->
                        new RuntimeException("Asset not found:" + assetId)
                        );

        UserEntity inspector = userEntityRepository.findById(inspectorUserId)
                .orElseThrow(()->
                        new RuntimeException("Inspector not found:" + inspectorUserId)
                );

        GeometryFactory geometryFactory = new GeometryFactory();

        Point geoTag = geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
        );
        geoTag.setSRID(4326);


        InspectionEntity inspection = InspectionEntity.builder()
                .assetId(asset)
                .inspectorUserId(inspector)
                .notes(notes)
                .geoTag(geoTag)
                .build();

        InspectionEntity savedInspection = inspectionRepository.save(inspection);

        asset.setLastInspectionDate(LocalDateTime.now());

        assetRepository.save(asset);

        return savedInspection;
    }
    public List<InspectionEntity> getAllInspections(){
        return inspectionRepository.findAll();
    }

    public InspectionEntity getInspectionById(UUID id){
        return inspectionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Inspection Not Found: " + id)
                );
    }
    public List<InspectionEntity> getInspectionsByAssetId(UUID assetId){
        return inspectionRepository.findByAssetId_Id(assetId);
    }
}
