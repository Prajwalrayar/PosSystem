package com.capstone.payload.StoreAnalysis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BranchSalesDto {
    private String branchName;
    private Double totalSales;
}
