package com.schoolerp.usermanagement.modules.Complaint.responseDto;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
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
public class ComplaintResponseDto {


    private String title;

    private String description;

}
