package com.capstone.controllers;

import com.capstone.payload.StoreAnalysis.*;
import com.capstone.service.StoreAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/analytics")
public class StoreAnalyticsController {

    private final StoreAnalyticsService storeAnalyticsService;

    // ✨ Store Overview (KPI Summary)
    @GetMapping("/{storeAdminId}/overview")
    public ResponseEntity<StoreOverviewDto> getStoreOverview(@PathVariable Long storeAdminId) {
        return ResponseEntity.ok(storeAnalyticsService.getStoreOverview(storeAdminId)); // implement logic if needed
    }

    // 📊 Sales Trends by Time (daily/weekly/monthly)
    @GetMapping("/{storeAdminId}/sales-trends")
    public ResponseEntity<TimeSeriesDataDto> getSalesTrends(@PathVariable Long storeAdminId,
                                                            @RequestParam String period) {
        return ResponseEntity.ok(storeAnalyticsService.getSalesTrends(storeAdminId, period)); // implement logic if needed
    }

    // 📅 Monthly Sales Chart (line)
    @GetMapping("/{storeAdminId}/sales/monthly")
    public ResponseEntity<List<TimeSeriesPointDto>> getMonthlySales(@PathVariable Long storeAdminId) {
        return ResponseEntity.ok(storeAnalyticsService.getMonthlySalesGraph(storeAdminId));
    }

    // 🗓️ Daily Sales Chart (line)
    @GetMapping("/{storeAdminId}/sales/daily")
    public ResponseEntity<List<TimeSeriesPointDto>> getDailySales(@PathVariable Long storeAdminId) {
        return ResponseEntity.ok(storeAnalyticsService.getDailySalesGraph(storeAdminId)); // implement logic if needed
    }


    // 📚 Sales by Product Category (pie/bar)
//    @GetMapping("/{storeAdminId}/sales/category")
//    public List<CategorySalesDTO> getSalesByCategory(@PathVariable Long storeAdminId) {
//        return storeAnalyticsService.getSalesByCategory(storeAdminId);
//    }

    // 💳 Sales by Payment Method (pie)
    @GetMapping("/{storeAdminId}/sales/payment-method")
    public ResponseEntity<List<PaymentInsightDto>> getSalesByPaymentMethod(@PathVariable Long storeAdminId) {
        return ResponseEntity.ok(storeAnalyticsService.getSalesByPaymentMethod(storeAdminId));
    }

    // 📍 Sales by Branch (bar)
    @GetMapping("/{storeAdminId}/sales/branch")
    public ResponseEntity<List<BranchSalesDto>> getSalesByBranch(@PathVariable Long storeAdminId) {
        return ResponseEntity.ok(storeAnalyticsService.getSalesByBranch(storeAdminId));
    }

    // 💵 Payment Breakdown (Cash, UPI, Card)
    @GetMapping("/{storeAdminId}/payments")
    public ResponseEntity<List<PaymentInsightDto>> getPaymentBreakdown(@PathVariable Long storeAdminId) {
        return ResponseEntity.ok(storeAnalyticsService.getPaymentBreakdown(storeAdminId));
    }

    // 🏘️ Branch Performance
    @GetMapping("/{storeAdminId}/branch-performance")
    public ResponseEntity<BranchPerformanceDto> getBranchPerformance(@PathVariable Long storeAdminId) {
        return ResponseEntity.ok(storeAnalyticsService.getBranchPerformance(storeAdminId));
    }


    // ⚠️ Alerts and Health Monitoring
    @GetMapping("/{storeAdminId}/alerts")
    public ResponseEntity<StoreAlertDto> getStoreAlerts(@PathVariable Long storeAdminId) {
        return ResponseEntity.ok(storeAnalyticsService.getStoreAlerts(storeAdminId));
    }
}
