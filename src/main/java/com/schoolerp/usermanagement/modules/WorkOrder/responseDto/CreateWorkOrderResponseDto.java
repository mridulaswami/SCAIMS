package com.schoolerp.usermanagement.modules.WorkOrder.responseDto;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkOrderResponseDto {

    private UUID id;
    private UUID complaintId;
    private UUID inspectorId;
    private WorkOrderEntity.Priority priority;
    private WorkOrderEntity.Status status;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
}
