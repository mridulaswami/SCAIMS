package com.schoolerp.usermanagement.modules.inspection.requestDto;

import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateInspectionRequest {
    private UUID assetId;
    private UUID inspectorUserId;
    private String notes;
    private double latitude;
    private double longitude;
}
