package com.schoolerp.usermanagement.modules.inspection.responseDto;

import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectorStatusChangeResponseDto {

    private InspectionEntity.Status status;
    private String workReport;
}
