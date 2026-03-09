package com.capstone.payload.StoreAnalysis;

import com.capstone.domain.PaymentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInsightDto {
    private PaymentType paymentMethod; // Cash, UPI, Card, Wallet
    private Double totalAmount;
}
