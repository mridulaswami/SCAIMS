package com.schoolerp.usermanagement.modules.Geometry;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Component;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Component
public class GeometryService {

        private static final int SRID = 4326;
        private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

        public Geometry toJtsGeometry(GeometryDto dto) {
            log.info("Received geometry dto: {}", dto);
            if (dto == null || dto.getType() == null) {
                throw new IllegalArgumentException("Geometry type must not be null");
            }

            return switch (dto.getType()) {
                case "Point" -> toPoint(dto.getCoordinates());
                case "LineString" -> toLineString(dto.getCoordinates());
                case "Polygon" -> toPolygon(dto.getCoordinates());
                default -> throw new IllegalArgumentException("Unsupported geometry type: " + dto.getType());
            };
        }

        private Point toPoint(Object coordinates) {
            List<Double> coords = (List<Double>) coordinates;
            Coordinate coordinate = new Coordinate(coords.get(0), coords.get(1));
            return geometryFactory.createPoint(coordinate);
        }

        private LineString toLineString(Object coordinates) {
            List<List<Double>> coords = (List<List<Double>>) coordinates;
            Coordinate[] coordinateArray = coords.stream()
                    .map(pair -> new Coordinate(pair.get(0), pair.get(1)))
                    .toArray(Coordinate[]::new);
            return geometryFactory.createLineString(coordinateArray);
        }

        private Polygon toPolygon(Object coordinates) {
            List<List<List<Double>>> coords = (List<List<List<Double>>>) coordinates;
            List<List<Double>> outerRing = coords.get(0);

            Coordinate[] ringCoordinates = outerRing.stream()
                    .map(pair -> new Coordinate(pair.get(0), pair.get(1)))
                    .toArray(Coordinate[]::new);

            LinearRing linearRing = geometryFactory.createLinearRing(ringCoordinates);
            return geometryFactory.createPolygon(linearRing);
        }
    }

