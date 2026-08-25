package com.schoolerp.usermanagement.modules.WorkOrder.requestDto;

import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeRequestDto {

    @NotNull(message = "Id is Required")
    private UUID id;

    @NotNull(message = "Status is Required")
    private WorkOrderEntity.Status status;

    private String workReport;

    private List<MultipartFile> photos;
}