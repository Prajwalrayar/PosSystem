package com.capstone.service;

import com.capstone.payload.StoreAnalysis.*;

import java.util.List;

public interface StoreAnalyticsService {
    StoreOverviewDto getStoreOverview(Long storeAdminId);

    TimeSeriesDataDto getSalesTrends(Long storeAdminId, String period);

    List<TimeSeriesPointDto> getMonthlySalesGraph(Long storeAdminId);

    List<TimeSeriesPointDto> getDailySalesGraph(Long storeAdminId);

//    List<CategorySalesDTO> getSalesByCategory(Long storeAdminId);

    List<PaymentInsightDto> getSalesByPaymentMethod(Long storeAdminId);

    List<BranchSalesDto> getSalesByBranch(Long storeAdminId);

    // 💵 Total amounts grouped by payment methods (Cash, UPI, Card)
    List<PaymentInsightDto> getPaymentBreakdown(Long storeAdminId);

    BranchPerformanceDto getBranchPerformance(Long storeAdminId);

    StoreAlertDto getStoreAlerts(Long storeAdminId);
}

