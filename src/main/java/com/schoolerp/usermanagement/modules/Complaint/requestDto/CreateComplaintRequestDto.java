package com.schoolerp.usermanagement.modules.Complaint.requestDto;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
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
@AllArgsConstructor
@NoArgsConstructor
public class CreateComplaintRequestDto {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private UUID asset;

    private String location;

    private List<MultipartFile> photos;
}
