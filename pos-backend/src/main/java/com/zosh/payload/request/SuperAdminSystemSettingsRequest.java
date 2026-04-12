package com.zosh.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperAdminSystemSettingsRequest {
    @NotNull
    private Boolean autoApproveStores;
    @NotNull
    private Boolean requireDocumentVerification;
    @NotNull
    private Boolean commissionAutoCalculation;
    @NotNull
    private Boolean maintenanceMode;
}
