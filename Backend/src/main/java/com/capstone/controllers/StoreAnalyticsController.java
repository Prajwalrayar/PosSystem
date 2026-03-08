package com.capstone.controllers;

import com.capstone.payload.StoreAnalysis.*;
import com.capstone.service.StoreAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/analytics")
public class StoreAnalyticsController {
    private final StoreAnalyticsService storeAnalyticsService;

    // ✨ Store Overview (KPI Summary)
    @GetMapping("/{storeAdminId}/overview")
    public StoreOverviewDto getStoreOverview(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getStoreOverview(storeAdminId);
    }

    // 📊 Sales Trends by Time (daily/weekly/monthly)
    @GetMapping("/{storeAdminId}/sales-trends")
    public TimeSeriesDataDto getSalesTrends(@PathVariable Long storeAdminId,
                                            @RequestParam String period) {
        return storeAnalyticsService.getSalesTrends(storeAdminId, period); // implement logic if needed
    }

    // 📅 Monthly Sales Chart (line)
    @GetMapping("/{storeAdminId}/sales/monthly")
    public List<TimeSeriesPointDto> getMonthlySales(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getMonthlySalesGraph(storeAdminId);
    }

    // 🗓️ Daily Sales Chart (line)
    @GetMapping("/{storeAdminId}/sales/daily")
    public List<TimeSeriesPointDto> getDailySales(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getDailySalesGraph(storeAdminId);
    }

    // 📚 Sales by Product Category (pie/bar)
    @GetMapping("/{storeAdminId}/sales/category")
    public List<CategorySalesDTO> getSalesByCategory(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getSalesByCategory(storeAdminId);
    }

    // 💳 Sales by Payment Method (pie)
    @GetMapping("/{storeAdminId}/sales/payment-method")
    public List<PaymentInsightDto> getSalesByPaymentMethod(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getSalesByPaymentMethod(storeAdminId);
    }

    // 📍 Sales by Branch (bar)
    @GetMapping("/{storeAdminId}/sales/branch")
    public List<BranchSalesDto> getSalesByBranch(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getSalesByBranch(storeAdminId);
    }

    // 💵 Payment Breakdown (Cash, UPI, Card)
    @GetMapping("/{storeAdminId}/payments")
    public List<PaymentInsightDto> getPaymentBreakdown(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getPaymentBreakdown(storeAdminId);
    }

    // 🏘️ Branch Performance
    @GetMapping("/{storeAdminId}/branch-performance")
    public BranchPerformanceDto getBranchPerformance(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getBranchPerformance(storeAdminId);
    }

    // ⚠️ Alerts and Health Monitoring
    @GetMapping("/{storeAdminId}/alerts")
    public StoreAlertDto getStoreAlerts(@PathVariable Long storeAdminId) {
        return storeAnalyticsService.getStoreAlerts(storeAdminId);
    }
}
