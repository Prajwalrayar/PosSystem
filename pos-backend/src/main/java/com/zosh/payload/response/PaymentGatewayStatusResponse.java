package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGatewayStatusResponse {
    private String gateway;
    private boolean configured;
    private boolean upiSupported;
    private boolean cardSupported;
    private List<String> missingFields;
    private String message;
}
