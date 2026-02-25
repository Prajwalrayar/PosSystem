package com.capstone.service.impl;

import com.capstone.domain.StoreStatus;
import com.capstone.payload.AdminAnalysis.DashboardSummaryDto;
import com.capstone.payload.AdminAnalysis.StoreRegistrationStatusDto;
import com.capstone.payload.AdminAnalysis.StoreStatusDistributionDto;
import com.capstone.repository.StoreRepository;
import com.capstone.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final StoreRepository storeRepository;

    @Override
    public DashboardSummaryDto getDashboardSummary() {
        Long total = storeRepository.count();
        Long active = storeRepository.countByStatus(StoreStatus.ACTIVE);
        Long pending = storeRepository.countByStatus(StoreStatus.PENDING);
        Long blocked = storeRepository.countByStatus(StoreStatus.BLOCKED);

        return DashboardSummaryDto.builder()
                .totalStores(total)
                .activeStores(active)
                .pendingStores(pending)
                .blockedStores(blocked)
                .build();
    }

    @Override
    public List<StoreRegistrationStatusDto> getLast7DayRegistrationStats() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = today.minusDays(6);
        List<Object[]> rawStats = storeRepository.getStoreRegistrationStats(sevenDaysAgo);

        Map<String, Long> dataMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Initialize 0 counts for 7 days
        for (int i = 0; i < 7; i++) {
            LocalDateTime date = sevenDaysAgo.plusDays(i);
            dataMap.put(date.format(formatter), 0L);
        }

        for (Object[] row : rawStats) {
            LocalDateTime date = (LocalDateTime) row[0];
            Long count = (Long) row[1];
            dataMap.put(date.format(formatter), count);
        }

        List<StoreRegistrationStatusDto> result = new ArrayList<>();
        dataMap.forEach((date, count) -> result.add(
                StoreRegistrationStatusDto.builder().date(date).count(count).build()
        ));

        return result;
    }

    @Override
    public StoreStatusDistributionDto getStoreStatusDistribution() {
        Long active = storeRepository.countByStatus(StoreStatus.ACTIVE);
        Long blocked = storeRepository.countByStatus(StoreStatus.BLOCKED);
        Long pending = storeRepository.countByStatus(StoreStatus.PENDING);

        return StoreStatusDistributionDto.builder()
                .active(active)
                .blocked(blocked)
                .pending(pending)
                .build();
    }
}
