package com.schoolerp.usermanagement.modules.Complaint.requestDto;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
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

    @NotNull
    private UUID citizenId;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private UUID asset;

    private String geometry;

    private List<MultipartFile> photos;
}
