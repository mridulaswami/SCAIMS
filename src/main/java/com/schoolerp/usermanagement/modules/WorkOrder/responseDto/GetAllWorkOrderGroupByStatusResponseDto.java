package com.schoolerp.usermanagement.modules.WorkOrder.responseDto;

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
public class GetAllWorkOrderGroupByStatusResponseDto {

    private String status;
    private List<WorkOrderComplaintResponseDto> workOrders;


    // =========================
    // Work Order + Complaint
    // =========================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkOrderComplaintResponseDto {

        private WorkOrderResponseDto workOrder;
        private ComplaintResponseDto complaint;
    }


    // =========================
    // Work Order Response
    // =========================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkOrderResponseDto {

        private UUID id;
        private UserEntity inspector;
        private String priority;
        private String status;
        private String workReport;
        private LocalDateTime dueDate;
        private LocalDateTime createdAt;
        private LocalDateTime closedAt;

        private List<String> photos;
    }


    // =========================
    // Complaint Response
    // =========================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplaintResponseDto {

        private UUID id;
        private UUID citizenId;
        private String title;
        private AssetResponseDto asset;
        private String description;

        private Object location;

        private String status;

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
