package com.schoolerp.usermanagement.modules.asset.AssetImport.components;

import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.OsmImportRequestDto;
import org.springframework.stereotype.Component;

@Component
public class QueryBuilder {

    public String overpassQuery(OsmImportRequestDto request){

        String bbox = "%f,%f,%f,%f".formatted(
               request.getSouth(),
                request.getWest(),
                request.getNorth(),
                request.getEast()
        );


        String query = """
    [out:json][timeout:60];
    (
      way["building"](%s);
      way["leisure"](%s);
      node["highway"="street_lamp"](%s);
      nwr["amenity"](%s);
    );
    out body geom;
    """.formatted(bbox, bbox, bbox, bbox);

        return query;

    }

}
