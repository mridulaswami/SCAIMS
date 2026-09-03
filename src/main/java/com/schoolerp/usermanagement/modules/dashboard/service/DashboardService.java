package com.schoolerp.usermanagement.modules.dashboard.service;

import com.schoolerp.usermanagement.modules.Complaint.entity.ComplaintEntity;
import com.schoolerp.usermanagement.modules.Complaint.repository.ComplaintRepository;
import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.WorkOrder.repository.WorkOrderEntityRepository;
import com.schoolerp.usermanagement.modules.asset.repository.AssetRepository;
import com.schoolerp.usermanagement.modules.dashboard.responseDto.AssetCategoryResponse;
import com.schoolerp.usermanagement.modules.dashboard.responseDto.ConditionBreakdownResponse;
import com.schoolerp.usermanagement.modules.dashboard.responseDto.DashboardResponse;
import com.schoolerp.usermanagement.modules.dashboard.responseDto.DashboardSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AssetRepository assetRepository;
    private final WorkOrderEntityRepository workOrderEntityRepository;
    private final ComplaintRepository complaintRepository;

    public DashboardResponse getDashboard() {

        // Total Assets
        long totalAssets = assetRepository.countAllAssets();

        // Open Work Orders
        long openWorkOrders = workOrderEntityRepository.countByStatusNot(WorkOrderEntity.Status.CLOSED);

        // Open Complaints
        long openComplaints = complaintRepository.countByStatusNot(ComplaintEntity.Status.COMPLETED);

        // Assets by Category
        List<AssetCategoryResponse> assetsByCategory = assetRepository.countAssetsByCategory().stream().map(row -> AssetCategoryResponse.builder().category((String) row[0]).count((Long) row[1]).build()).toList();

        // Assets by Condition
        List<ConditionBreakdownResponse> conditionBreakdown = assetRepository.countAssetsByCondition().stream().map(row -> ConditionBreakdownResponse.builder().condition((String) row[0]).count((Long) row[1]).build()).toList();

        // Dashboard Summary
        DashboardSummaryResponse summary = DashboardSummaryResponse.builder().totalAsset(totalAssets).openWorkOrders(openWorkOrders).openComplaints(openComplaints).build();

        // Final Dashboard Response
        return DashboardResponse.builder().summary(summary).assetsByCategory(assetsByCategory).conditionBreakdown(conditionBreakdown).build();
    }
}