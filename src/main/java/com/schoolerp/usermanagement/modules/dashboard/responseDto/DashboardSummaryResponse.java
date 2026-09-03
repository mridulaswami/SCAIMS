package com.schoolerp.usermanagement.modules.dashboard.responseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalAsset;

    private long openWorkOrders;

    private long overDueInspection;

    private long openComplaints;
}
