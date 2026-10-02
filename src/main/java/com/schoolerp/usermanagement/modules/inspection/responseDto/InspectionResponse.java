package com.schoolerp.usermanagement.modules.inspection.responseDto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class InspectionResponse {
    private UUID id;
    private UUID assetId;
    private UUID inspectorUserId;
    private String notes;
    private LocalDateTime inspectedAt;
    private Double latitude;
    private Double longitude;
    private List<String> photoUrls;


}