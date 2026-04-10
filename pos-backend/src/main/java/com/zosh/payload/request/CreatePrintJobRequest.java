package com.zosh.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePrintJobRequest {
    @NotBlank(message = "Type is required")
    private String type;
    @NotBlank(message = "Reference ID is required")
    private String referenceId;
    @NotBlank(message = "Printer ID is required")
    private String printerId;
}
