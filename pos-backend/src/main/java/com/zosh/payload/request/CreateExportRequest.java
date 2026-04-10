package com.zosh.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CreateExportRequest {
    @NotBlank(message = "Type is required")
    private String type;
    @NotBlank(message = "Format is required")
    private String format;
    private Map<String, Object> filters;
}
