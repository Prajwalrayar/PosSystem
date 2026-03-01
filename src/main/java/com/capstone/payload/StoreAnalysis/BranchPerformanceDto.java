package com.capstone.payload.StoreAnalysis;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BranchPerformanceDto {
    private List<BranchSalesDto> branchSales;
    private Integer newBranchesThisMonth;
    private String topBranch;
}
