package com.schoolerp.usermanagement.modules.Geometry;

import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeometryService {

    public Geometry convertToJts(GeometryDto dto) {

        String type = dto.getType();

        if(type =="Point"){
             List<Double> coordinates = (List<Double>) dto.getCoordinates();

        }

return null;

    }
}
