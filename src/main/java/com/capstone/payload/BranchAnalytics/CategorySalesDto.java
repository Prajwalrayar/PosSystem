package com.capstone.payload.BranchAnalytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySalesDto {
    private String categoryName;
    private Double totalSales;
    private Long quantitySold;
}
