package com.zosh.payload.request;

import com.zosh.domain.PaymentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletePaymentRequest {

    private Long customerId;

    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    private String customerEmail;

    private String customerPhone;

    @NotNull(message = "Payment method is required")
    private PaymentType paymentMethod;

    private String paymentReference;

    @Builder.Default
    private Boolean sendEmail = Boolean.TRUE;
}
