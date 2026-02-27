package com.capstone.controllers;

import com.capstone.payload.AdminAnalysis.DashboardSummaryDto;
import com.capstone.payload.AdminAnalysis.StoreRegistrationStatusDto;
import com.capstone.payload.AdminAnalysis.StoreStatusDistributionDto;
import com.capstone.service.AdminDashboardService;
import com.capstone.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/super-admin")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;
    private final StoreService storeService;

    /**
     * 📊 Get summary stats for dashboard cards
     * - 🏪 totalStores
     * - ✅ activeStores
     * - ⏳ pendingStores
     * - ⛔ blockedStores
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(adminDashboardService.getDashboardSummary());
    }

    /**
     * 📈 Get number of store registrations in the last 7 days
     * Used for 📅 chart data (line/bar)
     */
    @GetMapping("/dashboard/store-registrations")
    public ResponseEntity<List<StoreRegistrationStatusDto>> getLast7DayRegistrationStats() {
        return ResponseEntity.ok(adminDashboardService.getLast7DayRegistrationStats());
    }
    /**
     * 🥧 Get store status distribution
     * - ✅ active
     * - ⛔ blocked
     * - ⏳ pending
     * Used for pie chart 📊
     */
    @GetMapping("/dashboard/store-status-distribution")
    public ResponseEntity<StoreStatusDistributionDto> getStoreStatusDistribution() {
        return ResponseEntity.ok(adminDashboardService.getStoreStatusDistribution());
    }
}
