package com.zosh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.modal.SuperAdminSettings;
import com.zosh.payload.request.SuperAdminNotificationSettingsRequest;
import com.zosh.payload.request.SuperAdminSettingsRequest;
import com.zosh.payload.request.SuperAdminSystemSettingsRequest;
import com.zosh.payload.response.SuperAdminSettingsResponse;
import com.zosh.repository.SuperAdminSettingsRepository;
import com.zosh.service.SuperAdminSettingsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminSettingsServiceImpl implements SuperAdminSettingsService {

    private final SuperAdminSettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SuperAdminSettingsResponse getSettings() {
        return toResponse(ensureSettings());
    }

    @Override
    @Transactional
    public SuperAdminSettingsResponse updateNotificationSettings(SuperAdminNotificationSettingsRequest request) {
        SuperAdminSettings settings = ensureSettings();
        settings.setNotificationJson(writeJson(request));
        return toResponse(settingsRepository.save(settings));
    }

    @Override
    @Transactional
    public SuperAdminSettingsResponse updateSystemSettings(SuperAdminSystemSettingsRequest request) {
        SuperAdminSettings settings = ensureSettings();
        settings.setSystemJson(writeJson(request));
        return toResponse(settingsRepository.save(settings));
    }

    @Override
    @Transactional
    public SuperAdminSettingsResponse updateSettings(SuperAdminSettingsRequest request) {
        SuperAdminSettings settings = ensureSettings();
        if (request.getNotificationSettings() != null) {
            settings.setNotificationJson(writeJson(request.getNotificationSettings()));
        }
        if (request.getSystemSettings() != null) {
            settings.setSystemJson(writeJson(request.getSystemSettings()));
        }
        return toResponse(settingsRepository.save(settings));
    }

    private SuperAdminSettings ensureSettings() {
        return settingsRepository.findById(SuperAdminSettings.SETTINGS_ID)
                .orElseGet(() -> {
                    SuperAdminSettings settings = new SuperAdminSettings();
                    settings.setId(SuperAdminSettings.SETTINGS_ID);
                    settings.setNotificationJson(writeJson(defaultNotificationSettings()));
                    settings.setSystemJson(writeJson(defaultSystemSettings()));
                    return settingsRepository.save(settings);
                });
    }

    private SuperAdminSettingsResponse toResponse(SuperAdminSettings settings) {
        return new SuperAdminSettingsResponse(
                readJson(settings.getNotificationJson(), SuperAdminNotificationSettingsRequest.class, defaultNotificationSettings()),
                readJson(settings.getSystemJson(), SuperAdminSystemSettingsRequest.class, defaultSystemSettings()),
                settings.getUpdatedAt()
        );
    }

    private SuperAdminNotificationSettingsRequest defaultNotificationSettings() {
        SuperAdminNotificationSettingsRequest request = new SuperAdminNotificationSettingsRequest();
        request.setNewStoreRequests(true);
        request.setStoreApprovals(true);
        request.setCommissionUpdates(false);
        request.setSystemAlerts(true);
        request.setEmailNotifications(true);
        return request;
    }

    private SuperAdminSystemSettingsRequest defaultSystemSettings() {
        SuperAdminSystemSettingsRequest request = new SuperAdminSystemSettingsRequest();
        request.setAutoApproveStores(false);
        request.setRequireDocumentVerification(true);
        request.setCommissionAutoCalculation(true);
        request.setMaintenanceMode(false);
        return request;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize super admin settings", ex);
        }
    }

    private <T> T readJson(String value, Class<T> type, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException ex) {
            return fallback;
        }
    }
}
