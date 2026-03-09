package com.capstone.payload.StoreAnalysis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorySalesDTO {
    private String categoryName;
    private Double totalSales;

}
