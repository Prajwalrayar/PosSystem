package com.zosh.payload.StoreAnalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentBranchSalesDTO {
    private Long branchId;
    private String branchName;
    private Double totalSales;
    private LocalDateTime lastSaleAt;
    private String periodLabel;

    public RecentBranchSalesDTO(Long branchId, String branchName, Double totalSales, LocalDateTime lastSaleAt) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.totalSales = totalSales;
        this.lastSaleAt = lastSaleAt;
    }
}
