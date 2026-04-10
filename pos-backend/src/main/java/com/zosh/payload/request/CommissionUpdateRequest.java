package com.zosh.payload.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CommissionUpdateRequest {

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", message = "Rate must be at least 0")
    @DecimalMax(value = "10.0", message = "Rate must be at most 10")
    private BigDecimal rate;
}
