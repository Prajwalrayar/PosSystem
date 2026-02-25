package com.capstone.payload.AdminAnalysis;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreStatusDistributionDto {
    private Long active;
    private Long blocked;
    private Long pending;
}
