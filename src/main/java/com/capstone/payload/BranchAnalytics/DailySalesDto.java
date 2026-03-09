package com.capstone.payload.BranchAnalytics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailySalesDto {
    private LocalDate date;
    private BigDecimal totalSales;
}
