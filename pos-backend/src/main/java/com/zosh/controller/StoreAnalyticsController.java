package com.zosh.controller;

import com.zosh.payload.StoreAnalysis.*;
import com.zosh.service.StoreAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/analytics")
@RequiredArgsConstructor
public class StoreAnalyticsController {

    private final StoreAnalyticsService storeAnalyticsService;

    // ✨ Store Overview (KPI Summary)
    @GetMapping("/{storeAdminId}/overview")
    public StoreOverviewDTO getStoreOverview(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getStoreOverview(storeAdminId);
    }

    // 📊 Sales Trends by Time (daily/weekly/monthly)
    @GetMapping("/{storeAdminId}/sales-trends")
    public TimeSeriesDataDTO getSalesTrends(@PathVariable("storeAdminId") Long storeAdminId,
                                            @RequestParam("period") String period) {
        return storeAnalyticsService.getSalesTrends(storeAdminId, period); // implement logic if needed
    }

    // 📅 Monthly Sales Chart (line)
    @GetMapping("/{storeAdminId}/sales/monthly")
    public List<TimeSeriesPointDTO> getMonthlySales(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getMonthlySalesGraph(storeAdminId);
    }

    // 🗓️ Daily Sales Chart (line)
    @GetMapping("/{storeAdminId}/sales/daily")
    public List<TimeSeriesPointDTO> getDailySales(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getDailySalesGraph(storeAdminId);
    }

    // 📚 Sales by Product Category (pie/bar)
    @GetMapping("/{storeAdminId}/sales/category")
    public List<CategorySalesDTO> getSalesByCategory(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getSalesByCategory(storeAdminId);
    }

    // 💳 Sales by Payment Method (pie)
    @GetMapping("/{storeAdminId}/sales/payment-method")
    public List<PaymentInsightDTO> getSalesByPaymentMethod(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getSalesByPaymentMethod(storeAdminId);
    }

    @GetMapping("/{storeAdminId}/recent-sales")
    public List<RecentBranchSalesDTO> getRecentSales(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getRecentSales(storeAdminId);
    }

    // 📍 Sales by Branch (bar)
    @GetMapping("/{storeAdminId}/sales/branch")
    public List<BranchSalesDTO> getSalesByBranch(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getSalesByBranch(storeAdminId);
    }

    // 💵 Payment Breakdown (Cash, UPI, Card)
    @GetMapping("/{storeAdminId}/payments")
    public List<PaymentInsightDTO> getPaymentBreakdown(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getPaymentBreakdown(storeAdminId);
    }

    // 🏘️ Branch Performance
    @GetMapping("/{storeAdminId}/branch-performance")
    public BranchPerformanceDTO getBranchPerformance(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getBranchPerformance(storeAdminId);
    }

    // ⚠️ Alerts and Health Monitoring
    @GetMapping("/{storeAdminId}/alerts")
    public StoreAlertDTO getStoreAlerts(@PathVariable("storeAdminId") Long storeAdminId) {
        return storeAnalyticsService.getStoreAlerts(storeAdminId);
    }
}
