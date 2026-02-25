package com.capstone.payload.AdminAnalysis;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {
    private Long totalStores;
    private Long activeStores;
    private Long blockedStores;
    private Long pendingStores;
}
