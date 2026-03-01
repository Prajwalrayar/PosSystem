package com.capstone.payload.StoreAnalysis;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TimeSeriesDataDto {
    private List<TimeSeriesPointDto> points;
    private String period; // DAILY, WEEKLY, MONTHLY
}
