package com.zosh.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitiateReturnRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    @NotNull(message = "Cashier ID is required")
    private Long cashierId;
    @NotNull(message = "Branch ID is required")
    private Long branchId;
}
