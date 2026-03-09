package com.capstone.payload.dto;

import com.capstone.domain.PaymentType;
import com.capstone.model.Order;
import com.capstone.model.ShiftReport;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RefundDto {
    private Long id;

    private OrderDto order;
    private Long orderId;

    private String reason;
    private Double amount;

    private Long shiftReportId;

    private UserDto cashier;
    private String cashierName;

    private BranchDto branch;
    private Long branchId;

    private PaymentType paymentType;
    private LocalDateTime createdAt;
}
