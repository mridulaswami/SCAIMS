package com.schoolerp.usermanagement.modules.WorkOrder.requestDto;

import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateWorkOrderRequestDto {

    private UUID complaintId;

    private UUID inspectorId;

    private WorkOrderEntity.Priority priority;

    private LocalDateTime dueDate;

}
