package com.capstone.service;

import com.capstone.model.PaymentSummary;
import com.capstone.payload.BranchAnalytics.*;

import java.time.LocalDate;
import java.util.List;

public interface BranchAnalyticsService {
    List<DailySalesDto> getDailySalesChart(Long branchId, int days);
    List<ProductPerformanceDto> getTopProductsByQuantityWithPercentage(Long branchId);
    List<CashierPerformanceDto> getTopCashierPerformanceByOrders(Long branchId);
    List<CategorySalesDto> getCategoryWiseSalesBreakdown(Long branchId,
                                                         LocalDate date);

    BranchDashboardOverviewDto getBranchOverview(Long branchId);
    List<PaymentSummary> getPaymentMethodBreakdown(Long branchId, LocalDate date);
}
