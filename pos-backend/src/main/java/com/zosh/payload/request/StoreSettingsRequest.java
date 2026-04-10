package com.zosh.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StoreSettingsRequest {

    @Valid
    private StoreProfileSettings storeSettings;

    @Valid
    private NotificationSettings notificationSettings;

    @Valid
    private SecuritySettings securitySettings;

    @Valid
    private PaymentSettings paymentSettings;

    @Getter
    @Setter
    public static class StoreProfileSettings {
        @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
        private String storeName;
        @Email(message = "Store email must be valid")
        private String storeEmail;
        @Pattern(regexp = "^$|^(\\+?[1-9]\\d{1,14}|[0-9]{10,15})$", message = "Store phone must be valid")
        private String storePhone;
        @Size(max = 255, message = "Store address must be at most 255 characters")
        private String storeAddress;
        @Size(max = 500, message = "Store description must be at most 500 characters")
        private String storeDescription;
        @Size(max = 10, message = "Currency must be at most 10 characters")
        private String currency;
        @DecimalMin(value = "0.0", message = "Tax rate must be at least 0")
        @DecimalMax(value = "100.0", message = "Tax rate must be at most 100")
        private BigDecimal taxRate;
        @Size(max = 60, message = "Timezone must be at most 60 characters")
        private String timezone;
        @Size(max = 30, message = "Date format must be at most 30 characters")
        private String dateFormat;
        @Size(max = 255, message = "Receipt footer must be at most 255 characters")
        private String receiptFooter;
    }

    @Getter
    @Setter
    public static class NotificationSettings {
        private Boolean emailNotifications;
        private Boolean smsNotifications;
        private Boolean lowStockAlerts;
        private Boolean salesReports;
        private Boolean employeeActivity;
    }

    @Getter
    @Setter
    public static class SecuritySettings {
        private Boolean twoFactorAuth;
        @Min(value = 1, message = "Password expiry must be at least 1 day")
        @Max(value = 365, message = "Password expiry must be at most 365 days")
        private Integer passwordExpiry;
        @Min(value = 1, message = "Session timeout must be at least 1 minute")
        @Max(value = 1440, message = "Session timeout must be at most 1440 minutes")
        private Integer sessionTimeout;
        private Boolean ipRestriction;
    }

    @Getter
    @Setter
    public static class PaymentSettings {
        private Boolean acceptCash;
        private Boolean acceptCredit;
        private Boolean acceptDebit;
        private Boolean acceptMobile;
        private Boolean stripeEnabled;
        private Boolean paypalEnabled;
    }
}
