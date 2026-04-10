package com.zosh.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BranchSettingsRequest {

    @Valid
    private PrinterSettings printer;

    @Valid
    private TaxSettings tax;

    @Valid
    private PaymentSettings payment;

    @Valid
    private DiscountSettings discount;

    @Getter
    @Setter
    public static class PrinterSettings {
        @Size(max = 100, message = "Printer name must be at most 100 characters")
        private String printerName;
        @Pattern(regexp = "^(58mm|80mm|A4)?$", message = "Paper size must be 58mm, 80mm, or A4")
        private String paperSize;
        private Boolean printLogo;
        private Boolean printCustomerDetails;
        private Boolean printItemizedTax;
        @Size(max = 255, message = "Footer text must be at most 255 characters")
        private String footerText;
    }

    @Getter
    @Setter
    public static class TaxSettings {
        private Boolean gstEnabled;
        @DecimalMin(value = "0.0", message = "GST percentage must be at least 0")
        @DecimalMax(value = "100.0", message = "GST percentage must be at most 100")
        private BigDecimal gstPercentage;
        private Boolean applyGstToAll;
        private Boolean showTaxBreakdown;
    }

    @Getter
    @Setter
    public static class PaymentSettings {
        private Boolean acceptCash;
        private Boolean acceptUPI;
        private Boolean acceptCard;
        @Size(max = 100, message = "UPI ID must be at most 100 characters")
        private String upiId;
        @Size(max = 100, message = "Card terminal ID must be at most 100 characters")
        private String cardTerminalId;
    }

    @Getter
    @Setter
    public static class DiscountSettings {
        private Boolean allowDiscount;
        @Min(value = 0, message = "Max discount percentage must be at least 0")
        @Max(value = 100, message = "Max discount percentage must be at most 100")
        private Integer maxDiscountPercentage;
        private Boolean requireManagerApproval;
        private List<@Size(max = 100, message = "Discount reason must be at most 100 characters") String> discountReasons;
    }
}
