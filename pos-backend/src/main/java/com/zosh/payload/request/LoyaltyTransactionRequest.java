package com.zosh.payload.request;

import com.zosh.modal.LoyaltyTransactionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoyaltyTransactionRequest {

    @NotNull(message = "Type is required")
    private LoyaltyTransactionType type;

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be greater than 0")
    @Max(value = 5000, message = "Points must be less than or equal to 5000")
    private Integer points;

    @NotBlank(message = "Reason is required")
    @Size(max = 64, message = "Reason must be at most 64 characters")
    private String reason;

    @Size(max = 255, message = "Note must be at most 255 characters")
    private String note;
}
