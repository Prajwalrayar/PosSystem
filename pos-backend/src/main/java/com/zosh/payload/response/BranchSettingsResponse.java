package com.zosh.payload.response;

import com.zosh.payload.request.BranchSettingsRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BranchSettingsResponse {
    private Long branchId;
    private BranchSettingsRequest.PrinterSettings printer;
    private BranchSettingsRequest.TaxSettings tax;
    private BranchSettingsRequest.PaymentSettings payment;
    private BranchSettingsRequest.DiscountSettings discount;
    private LocalDateTime updatedAt;
}
