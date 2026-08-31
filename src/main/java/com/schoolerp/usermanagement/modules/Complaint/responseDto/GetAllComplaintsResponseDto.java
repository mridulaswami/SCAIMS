package com.schoolerp.usermanagement.modules.Complaint.responseDto;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintPhotosEntity;
import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
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
public class GetAllComplaintsResponseDto {

    private UUID id;

    private UUID citizenId;

    private UUID assetId;

    private String title;

    private String description;

    private String status;

    private GeometryDto location;

    private List<String> photos;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}