package com.schoolerp.usermanagement.modules.asset.AssetImport.service;

import com.schoolerp.usermanagement.modules.asset.AssetImport.components.OverpassClient;
import com.schoolerp.usermanagement.modules.asset.AssetImport.components.OverpassJsonParser;
import com.schoolerp.usermanagement.modules.asset.AssetImport.components.QueryBuilder;
import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.GeometryPointDto;
import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.OsmImportRequestDto;
import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.OverPassResponseDto;
import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.OverpassElementDto;
import com.schoolerp.usermanagement.modules.asset.AssetImport.repository.AssetImportRepository;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.entity.ParentAssetEntity;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import com.schoolerp.usermanagement.modules.assetCategory.repository.AssetCategoryRepository;
import com.schoolerp.usermanagement.modules.notification.constant.NotificationTitleConstant;
import com.schoolerp.usermanagement.modules.notification.enums.NotificationPriority;
import com.schoolerp.usermanagement.modules.notification.enums.NotificationType;
import com.schoolerp.usermanagement.modules.notification.enums.TargetType;
import com.schoolerp.usermanagement.modules.notification.event.NotificationEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OsmImportServiceImpl implements OsmImportService{

    private final QueryBuilder queryBuilder;
    private final OverpassClient overpassClient;
    private final OverpassJsonParser overpassJsonParser;
    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetImportRepository assetImportRepository;
    private final AssetRepository assetRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);


    @Override
    @Transactional
    public Integer importAssets(OsmImportRequestDto request) {

        String query = queryBuilder.overpassQuery(request);
        log.info("Overpass Query: {}", query);

        String fetchRawData = overpassClient.fetchRaw(query);
        log.info("Fetched Data: {}", fetchRawData);

        OverPassResponseDto response = overpassJsonParser.parse(fetchRawData);
     //   log.info("Parsed Response: {}", response);


        List<AssetEntity> entity = new ArrayList<>();

        Set<String> categories = new HashSet<>();
        List<OverpassElementDto> validElements = new ArrayList<>();


        for(OverpassElementDto elements : response.getElements()){

            String category = categoryResolver(elements.getTags());
            if(category == null) continue;

            categories.add(category);

            GeometryShape shape = shapeClassifier(elements);
            if(shape == GeometryShape.UNSUPPORTED) continue;

            validElements.add(elements);

//            String name = resolveName(elements, category);
//
//            AssetEntity asset = saveAsset(elements , category, shape, name);
//
//            entity.add(asset);
        }

        for(String name : categories) {
            String iconKey;
            if(name.equals("BUILDING")){
                iconKey="building";
            }else if(name.equals("PARK")){
                iconKey="park";
            }else if(name.equals("STREET LAMP")){
                iconKey="point";
            }else
                iconKey="amenity";

            assetCategoryRepository.upsert(name,iconKey);
        }

        assetCategoryRepository.flush();

    //    assetRepository.saveAll(entity);

        List<AssetEntity> assetentity = new ArrayList<>();
        for (OverpassElementDto elements : validElements) {
            String category = categoryResolver(elements.getTags());
            GeometryShape shape = shapeClassifier(elements);
            String name = resolveName(elements, category);
            assetentity.add(saveAsset(elements, category, shape, name));
        }
        long assetCount = assetentity.size();

        for(AssetEntity asset : assetentity){
            assetRepository.upsertAsset(
                    asset.getName(),
                    asset.getCategoryId().getId(),
                    asset.getGeometry().toText(),
                    asset.getSource_type(),
                    asset.getSource_id()

            );
        }


        assetRepository.flush();

        int linkedParents = assetImportRepository.backfillParentAssignments();

        // Send in-app notification to all admins
        try {
            String message = String.format("Successfully imported %d assets across %d categories (Linked parents: %d).",
                    assetentity.size(), categories.size(), linkedParents);

            notificationEventPublisher.publishToAdmins(
                    null,
                    NotificationTitleConstant.ASSET_IMPORT_COMPLETED,
                    message,
                    NotificationType.ASSET_IMPORTED,
                    NotificationPriority.MEDIUM,
                    TargetType.ASSET,
                    String.format("BBOX[%.4f,%.4f,%.4f,%.4f]", request.getSouth(), request.getWest(), request.getNorth(), request.getEast())
            );
            log.info("In-app notification sent to admins for OSM asset import: {}", message);
        } catch (Exception ex) {
            log.error("Failed to send in-app notification for asset import: {}", ex.getMessage(), ex);
        }

      //  long assetCount = assetImportRepository.count();

        return (int) assetCount;

    }


    private String categoryResolver(Map<String, String> tags){

        if(tags.containsKey("building")){
            return "BUILDING";
        } else if(tags.containsKey("leisure")){
            return "PARK";
        } else if(tags.containsKey("highway")){
            return "STREET LAMP";
        } else if(tags.containsKey("amenity")){
            return "AMENITY";
        }
        else if(tags.containsKey("healthcare")) {
            return "HEALTHCARE";
        }
        else {
            return "UNKNOWN";
        }

    }

    private enum GeometryShape {
        POINT,
        LINESTRING,
        POLYGON,
        UNSUPPORTED
    }

    private GeometryShape shapeClassifier(OverpassElementDto request){
     if(request.getType().equals("node")){
           return request.getLat() != null ? GeometryShape.POINT : GeometryShape.UNSUPPORTED;
     }
     if(request.getType().equals("way") && request.getGeometry().size()>=2){

         GeometryPointDto first = request.getGeometry().getFirst();
         GeometryPointDto last = request.getGeometry().getLast();

         if(request.getGeometry().size()>=4 && first.getLat() == last.getLat() && first.getLon() == last.getLon()){
             return GeometryShape.POLYGON;
         }else {
             return GeometryShape.LINESTRING;
         }

     }
     return GeometryShape.UNSUPPORTED;
    }

    private AssetEntity saveAsset(OverpassElementDto el, String category , GeometryShape shape, String name){

        UUID catId = assetImportRepository.getCatId(category);

        Geometry geom = switch(shape){
            case POINT -> gf.createPoint(new Coordinate(el.getLon(), el.getLat()));
            case POLYGON -> gf.createPolygon(toCoordinates(el));
            case LINESTRING -> gf.createLineString(toCoordinates(el));
            case UNSUPPORTED -> throw new IllegalStateException("Unsupported shape reached");
        };

        AssetEntity asset = new AssetEntity();
        asset.setName(name);
        asset.setCategoryId(assetCategoryRepository.getReferenceById(catId));
        asset.setGeometry(geom);
        asset.setSource_id(el.getId());
        asset.setSource_type(el.getType());
        return asset;

    }

    private Coordinate[] toCoordinates(OverpassElementDto el){
        return el.getGeometry().stream()
                .map(p-> new Coordinate(p.getLon(),p.getLat())).
                toArray(Coordinate[]::new);

    }


    private String resolveName(OverpassElementDto el, String category) {

        String tagName = el.getTags() != null ? el.getTags().get("name") :null;

      //  String osmName = el.getTags() != null ? "%s-%d".formatted(el.getTags().get("name"), el.getId()) : null;

        if(tagName != null && !tagName.isBlank()){
            return "%s-%d".formatted(tagName, el.getId());
        }

        return  "%s-%d".formatted(category, el.getId());
    }



}
