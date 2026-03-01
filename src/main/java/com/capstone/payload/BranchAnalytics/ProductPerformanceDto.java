package com.capstone.payload.BranchAnalytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductPerformanceDto {
    private String productName;
    private Long quantitySold;
    private double percentage;
}
