package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommissionResponse {
    private Long storeId;
    private String storeName;
    private BigDecimal previousRate;
    private BigDecimal currentRate;
    private LocalDateTime updatedAt;
}
