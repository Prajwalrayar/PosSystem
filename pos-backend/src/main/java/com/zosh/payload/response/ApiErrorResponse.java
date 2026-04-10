package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class ApiErrorResponse {

    private final boolean success = false;
    private final String message;
    private final List<FieldErrorResponse> errors;
    private final String timestamp;

    public ApiErrorResponse(String message, List<FieldErrorResponse> errors) {
        this.message = message;
        this.errors = errors;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Getter
    @AllArgsConstructor
    public static class FieldErrorResponse {
        private String field;
        private String message;
    }
}
