package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EmployeeActivityLogResponse {
    private LocalDateTime at;
    private String event;
}
