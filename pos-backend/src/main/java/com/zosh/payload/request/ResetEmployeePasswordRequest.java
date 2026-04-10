package com.zosh.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetEmployeePasswordRequest {

    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;

    private Boolean forceChangeOnNextLogin = Boolean.TRUE;
}
