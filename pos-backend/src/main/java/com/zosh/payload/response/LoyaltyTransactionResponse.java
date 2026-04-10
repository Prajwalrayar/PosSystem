package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoyaltyTransactionResponse {
    private Long customerId;
    private Integer pointsBefore;
    private Integer pointsChanged;
    private Integer pointsAfter;
    private Long transactionId;
}
