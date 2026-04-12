package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private Long invoiceId;
    private String invoiceNumber;
    private Long orderId;
    private String invoicePdfUrl;
    private String deliveryStatus;
    private LocalDateTime invoiceDateTime;
    private LocalDateTime emailSentAt;
    private Integer retryCount;
    private String lastError;
    private String customerEmail;
    private String paymentMethod;
    private String paymentReference;
    private String message;
}
