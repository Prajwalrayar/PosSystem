package com.zosh.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperAdminNotificationSettingsRequest {
    @NotNull
    private Boolean newStoreRequests;
    @NotNull
    private Boolean storeApprovals;
    @NotNull
    private Boolean commissionUpdates;
    @NotNull
    private Boolean systemAlerts;
    @NotNull
    private Boolean emailNotifications;
}
