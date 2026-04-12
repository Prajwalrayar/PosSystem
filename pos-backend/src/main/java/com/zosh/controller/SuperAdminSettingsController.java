package com.zosh.controller;

import com.zosh.payload.request.SuperAdminNotificationSettingsRequest;
import com.zosh.payload.request.SuperAdminSettingsRequest;
import com.zosh.payload.request.SuperAdminSystemSettingsRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.SuperAdminSettingsResponse;
import com.zosh.service.SuperAdminSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin/settings")
@RequiredArgsConstructor
public class SuperAdminSettingsController {

    private final SuperAdminSettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponseBody<SuperAdminSettingsResponse>> getSettings() {
        SuperAdminSettingsResponse response = settingsService.getSettings();
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Settings fetched", response));
    }

    @PatchMapping("/notifications")
    public ResponseEntity<ApiResponseBody<SuperAdminSettingsResponse>> updateNotificationSettings(
            @Valid @RequestBody SuperAdminNotificationSettingsRequest request
    ) {
        SuperAdminSettingsResponse response = settingsService.updateNotificationSettings(request);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Notification settings updated", response));
    }

    @PatchMapping("/system")
    public ResponseEntity<ApiResponseBody<SuperAdminSettingsResponse>> updateSystemSettings(
            @Valid @RequestBody SuperAdminSystemSettingsRequest request
    ) {
        SuperAdminSettingsResponse response = settingsService.updateSystemSettings(request);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "System settings updated", response));
    }

    @PatchMapping
    public ResponseEntity<ApiResponseBody<SuperAdminSettingsResponse>> updateSettings(
            @Valid @RequestBody SuperAdminSettingsRequest request
    ) {
        SuperAdminSettingsResponse response = settingsService.updateSettings(request);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Settings updated", response));
    }
}
