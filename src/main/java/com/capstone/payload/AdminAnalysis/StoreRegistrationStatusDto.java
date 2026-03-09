package com.capstone.payload.AdminAnalysis;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRegistrationStatusDto {
    private String date; // formatted as yyyy-MM-dd
    private Long count;
}
