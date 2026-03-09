package com.capstone.payload.AdminAnalysis;

import lombok.*;

@Data
@Builder
public class StoreStatusDistributionDto {
    private Long active;
    private Long blocked;
    private Long pending;
}
