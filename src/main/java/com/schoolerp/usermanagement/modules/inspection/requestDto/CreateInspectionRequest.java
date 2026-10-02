package com.schoolerp.usermanagement.modules.inspection.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;
import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateInspectionRequest {
    private UUID assetId;
    private UUID inspectorUserId;
    private String notes;
    private InspectionEntity.Priority priority;
    private LocalDateTime dueDate;
}