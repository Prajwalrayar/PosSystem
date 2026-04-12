package com.zosh.payload.request;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperAdminSettingsRequest {

    @Valid
    private SuperAdminNotificationSettingsRequest notificationSettings;

    @Valid
    private SuperAdminSystemSettingsRequest systemSettings;
}
