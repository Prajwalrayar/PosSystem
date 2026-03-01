package com.capstone.payload.StoreAnalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
public class TimeSeriesPointDto {
    private LocalDateTime date;
    private Double totalAmount;
}
