package com.capstone.model;

import com.capstone.domain.PaymentType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentSummary {

    private PaymentType type; // CASH, CARD, UPI

    private Double totalAmount;
    private int transactionCount;
    private double percentage;
}
