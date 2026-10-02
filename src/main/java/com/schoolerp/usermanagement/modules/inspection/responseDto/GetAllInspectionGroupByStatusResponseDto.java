package com.schoolerp.usermanagement.modules.inspection.responseDto;

import com.schoolerp.usermanagement.modules.Geometry.GeometryDto;
import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
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
public class GetAllInspectionGroupByStatusResponseDto {


    private String status;
    private List<InspectorResponseDto> inspections;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectorResponseDto {

        private UUID id;
        private UserEntity inspector;
        private String priority;
        private String status;
        private String workReport;
        private LocalDateTime dueDate;
        private AssetResponseDto asset;
        private List<String> photos;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetResponseDto {

        private UUID id;
        private String name;
        private String status;
        private String condition;
        private String ward;
        private GeometryDto geometry;
    }

}
