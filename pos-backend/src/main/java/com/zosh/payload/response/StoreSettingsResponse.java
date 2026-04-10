package com.zosh.payload.response;

import com.zosh.payload.request.StoreSettingsRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StoreSettingsResponse {
    private Long storeId;
    private StoreSettingsRequest.StoreProfileSettings storeSettings;
    private StoreSettingsRequest.NotificationSettings notificationSettings;
    private StoreSettingsRequest.SecuritySettings securitySettings;
    private StoreSettingsRequest.PaymentSettings paymentSettings;
    private LocalDateTime updatedAt;
}
