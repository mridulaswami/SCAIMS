package com.schoolerp.usermanagement.modules.inspection.requestDto;

import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
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
public class InspectorStatusChangeRequestDto {


    @NotNull(message = "Id is Required")
    private UUID id;

    @NotNull(message = "Status is Required")
    private InspectionEntity.Status status;

    private String workReport;

    private String rejectionReason;

    private List<MultipartFile> photos;
}
