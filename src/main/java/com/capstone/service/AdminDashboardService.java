package com.capstone.service;

import com.capstone.payload.AdminAnalysis.DashboardSummaryDto;
import com.capstone.payload.AdminAnalysis.StoreRegistrationStatusDto;
import com.capstone.payload.AdminAnalysis.StoreStatusDistributionDto;

import java.util.List;

public interface AdminDashboardService {

    DashboardSummaryDto getDashboardSummary();

    List<StoreRegistrationStatusDto> getLast7DayRegistrationStats();

    StoreStatusDistributionDto getStoreStatusDistribution();
}
