package com.schoolerp.usermanagement.modules.dashboard.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private DashboardSummaryResponse summary;

    private List<AssetCategoryResponse> assetsByCategory;

    private List<ConditionBreakdownResponse> conditionBreakdown;

    private List<RecentActivityResponse> recentActivities;
}
