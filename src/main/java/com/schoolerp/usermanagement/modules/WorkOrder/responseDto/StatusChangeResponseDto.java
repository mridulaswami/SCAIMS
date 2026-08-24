package com.schoolerp.usermanagement.modules.WorkOrder.responseDto;

import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeResponseDto {


    private WorkOrderEntity.Status status;
}
