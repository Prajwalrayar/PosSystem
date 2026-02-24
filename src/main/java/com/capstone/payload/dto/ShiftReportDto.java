package com.capstone.payload.dto;

import com.capstone.model.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShiftReportDto {

    private Long id;

    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;

    private Double totalSales;
    private Double totalRefunds;
    private Double netSales;
    private int totalOrders;

    private UserDto cashier;
    private Long cashierId;

    private BranchDto branch;
    private Long branchId;

    private List<PaymentSummary> paymentSummaries;
    private List<ProductDto> topSellingProducts;

    private List<OrderDto> recentOrders;

    private List<RefundDto> refunds;
}
