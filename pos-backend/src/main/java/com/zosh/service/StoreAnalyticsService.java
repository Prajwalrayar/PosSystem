package com.zosh.service;

import com.zosh.payload.StoreAnalysis.*;
import com.zosh.payload.response.StoreSettingsResponse;

import java.util.List;

public interface StoreAnalyticsService {
    StoreOverviewDTO getStoreOverview(Long storeAdminId);

    TimeSeriesDataDTO getSalesTrends(Long storeAdminId, String period);

    List<TimeSeriesPointDTO> getMonthlySalesGraph(Long storeAdminId);

    List<TimeSeriesPointDTO> getDailySalesGraph(Long storeAdminId);

    List<CategorySalesDTO> getSalesByCategory(Long storeAdminId);

    List<PaymentInsightDTO> getSalesByPaymentMethod(Long storeAdminId);

    List<RecentBranchSalesDTO> getRecentSales(Long storeAdminId);

    List<BranchSalesDTO> getSalesByBranch(Long storeAdminId);

    List<PaymentInsightDTO> getPaymentBreakdown(Long storeAdminId);

    BranchPerformanceDTO getBranchPerformance(Long storeAdminId);

    StoreAlertDTO getStoreAlerts(Long storeAdminId);
}
