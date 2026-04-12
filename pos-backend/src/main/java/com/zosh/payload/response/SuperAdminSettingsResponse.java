package com.zosh.payload.response;

import com.zosh.payload.request.SuperAdminNotificationSettingsRequest;
import com.zosh.payload.request.SuperAdminSystemSettingsRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SuperAdminSettingsResponse {
    private SuperAdminNotificationSettingsRequest notificationSettings;
    private SuperAdminSystemSettingsRequest systemSettings;
    private LocalDateTime updatedAt;
}
