package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmployeePerformanceResponse {
    private Long ordersProcessed;
    private Double totalSales;
    private Double averageOrderValue;
    private List<EmployeePerformanceDailySales> dailySales;
    private List<EmployeeActivityLogResponse> activityLog;

    @Getter
    @AllArgsConstructor
    public static class EmployeePerformanceDailySales {
        private String date;
        private Double amount;
    }
}
