package com.zosh.service;

import com.zosh.payload.request.SuperAdminNotificationSettingsRequest;
import com.zosh.payload.request.SuperAdminSettingsRequest;
import com.zosh.payload.request.SuperAdminSystemSettingsRequest;
import com.zosh.payload.response.SuperAdminSettingsResponse;

public interface SuperAdminSettingsService {
    SuperAdminSettingsResponse getSettings();

    SuperAdminSettingsResponse updateNotificationSettings(SuperAdminNotificationSettingsRequest request);

    SuperAdminSettingsResponse updateSystemSettings(SuperAdminSystemSettingsRequest request);

    SuperAdminSettingsResponse updateSettings(SuperAdminSettingsRequest request);
}
