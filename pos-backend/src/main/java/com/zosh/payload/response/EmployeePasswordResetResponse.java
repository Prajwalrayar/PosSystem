package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeePasswordResetResponse {
    private Long employeeId;
    private Boolean forceChangeOnNextLogin;
}
