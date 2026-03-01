package com.capstone.controllers;

import com.capstone.model.PaymentSummary;
import com.capstone.payload.BranchAnalytics.*;
import com.capstone.service.BranchAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branch-analytics")
public class BranchAnalyticsController {

    private final BranchAnalyticsService branchAnalyticsService;

    // ✅ Allow only BRANCH_MANAGER or BRANCH_ADMIN
    private static final String ALLOWED_ROLES = "hasRole('BRANCH_MANAGER') or hasRole('BRANCH_ADMIN')";

    @GetMapping("/daily-sales")
    @PreAuthorize(ALLOWED_ROLES)
    public ResponseEntity<List<DailySalesDto>> getDailySalesChart(
            @RequestParam Long branchId,
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(branchAnalyticsService.getDailySalesChart(branchId, days));
    }

    @GetMapping("/top-products")
    @PreAuthorize(ALLOWED_ROLES)
    public ResponseEntity<List<ProductPerformanceDto>> getTopProductsByQuantity(@RequestParam Long branchId) {
        return ResponseEntity.ok(branchAnalyticsService.getTopProductsByQuantityWithPercentage(branchId));
    }

    @GetMapping("/top-cashiers")
    @PreAuthorize(ALLOWED_ROLES)
    public ResponseEntity<List<CashierPerformanceDto>> getTopCashiersByRevenue(
            @RequestParam Long branchId
    ) {
        return ResponseEntity.ok(branchAnalyticsService.getTopCashierPerformanceByOrders(branchId));
    }

    /**
     * Get category-wise sales breakdown
     */
    @GetMapping("/category-sales")
    @PreAuthorize(ALLOWED_ROLES)
    public ResponseEntity<List<CategorySalesDto>> getCategoryWiseSalesBreakdown(@RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(branchAnalyticsService.getCategoryWiseSalesBreakdown(branchId, date));
    }

    @GetMapping("/today-overview")
    @PreAuthorize("hasRole('BRANCH_MANAGER') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<BranchDashboardOverviewDto> getTodayOverview(
            @RequestParam Long branchId
    ) {
        return ResponseEntity.ok(branchAnalyticsService.getBranchOverview(branchId));
    }


    @GetMapping("/payment-breakdown")
    @PreAuthorize("hasRole('BRANCH_MANAGER') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<List<PaymentSummary>> getPaymentBreakdown(
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
                branchAnalyticsService.getPaymentMethodBreakdown(branchId, date)
        );
    }
}
