package com.capstone.payload.dto;

import com.capstone.domain.OrderStatus;
import com.capstone.domain.PaymentType;
import com.capstone.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private Double totalAmount;
    private LocalDateTime createdAt;

    private Long branchId;

    private Long customerId;

//    private Long cashierId;

    private BranchDto branch;

    private UserDto cashier;

    private Customer customer;

    private List<OrderItemDto> items;
    private PaymentType paymentType;
    private OrderStatus status;
}
