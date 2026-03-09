package com.capstone.payload.StoreAnalysis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreOverviewDto {
    private Integer totalBranches;
    private Double totalSales;
    private Integer totalOrders;
    private Integer totalEmployees;
    private Integer totalCustomers;
    private Integer totalRefunds;
    private Integer totalProducts;
    private String topBranchName;

}
