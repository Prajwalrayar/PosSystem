package com.capstone.payload.BranchAnalytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashierPerformanceDto {
    private Long cashierId;
    private String cashierName;
    private Long totalOrders;
    private Double totalRevenue;
}
