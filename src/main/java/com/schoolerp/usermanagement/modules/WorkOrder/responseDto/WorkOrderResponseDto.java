package com.schoolerp.usermanagement.modules.WorkOrder.responseDto;

import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderPhotoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderResponseDto {

    private UUID id;

    private UUID complaintId;

    private UUID inspectorId;

    private String priority;

    private String status;

    private String workReport;

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    private List<String> photos;
}