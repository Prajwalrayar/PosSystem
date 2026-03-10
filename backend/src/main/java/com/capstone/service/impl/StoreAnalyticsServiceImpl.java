package com.capstone.service.impl;

import com.capstone.domain.UserRole;
import com.capstone.model.Order;
import com.capstone.payload.StoreAnalysis.*;
import com.capstone.repository.*;
import com.capstone.service.StoreAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreAnalyticsServiceImpl implements StoreAnalyticsService {

    private final BranchRepository branchRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RefundRepository refundRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public StoreOverviewDto getStoreOverview(Long storeAdminId) {
        List<UserRole> roles = new ArrayList<>();
        roles.add(UserRole.ROLE_STORE_MANAGER);
        roles.add(UserRole.ROLE_BRANCH_MANAGER);
        roles.add(UserRole.ROLE_STORE_ADMIN);
        roles.add(UserRole.ROLE_BRANCH_CASHIER);
//        roles.add(UserRole.ROLE_CUSTOMER);

        return StoreOverviewDto.builder()
                .totalBranches(branchRepository.countByStoreAdminId(storeAdminId))
                .totalSales(orderRepository.sumTotalSalesByStoreAdmin(storeAdminId).orElse(Double.valueOf(0)))
                .totalOrders(orderRepository.countByStoreAdminId(storeAdminId))
                .totalEmployees(userRepository.countByStoreAdminIdAndRoles(storeAdminId,roles))
                .totalCustomers(customerRepository.countByStoreAdminId(storeAdminId))
                .totalRefunds(refundRepository.countByStoreAdminId(storeAdminId))
                .totalProducts(productRepository.countByStoreAdminId(storeAdminId))
//                .topBranchName(branchRepository.findTopBranchBySales(storeAdminId))
                .build();
    }

    @Override
    public TimeSeriesDataDto getSalesTrends(Long storeAdminId, String period) {
        return null;
    }

    @Override
    public List<TimeSeriesPointDto> getMonthlySalesGraph(Long storeAdminId) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(365);

        List<Order> orders = orderRepository.findAllByStoreAdminAndCreatedAtBetween(storeAdminId, start, end);

        Map<YearMonth, Double> grouped = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> YearMonth.from(order.getCreatedAt()),  // Group by Year-Month
                        Collectors.summingDouble(order ->
                                order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0
                        )
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new TimeSeriesPointDto(
                        entry.getKey().atDay(1).atStartOfDay(), // Convert YearMonth to LocalDateTime
                        entry.getValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSeriesPointDto> getDailySalesGraph(Long storeAdminId) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(6);
        return orderRepository.getDailySales(storeAdminId, start, end);
    }

    @Override
    public List<PaymentInsightDto> getSalesByPaymentMethod(Long storeAdminId) {
        return orderRepository.getSalesByPaymentMethod(storeAdminId);
    }

    @Override
    public List<BranchSalesDto> getSalesByBranch(Long storeAdminId) {
        return orderRepository.getSalesByBranch(storeAdminId);
    }

    @Override
    public List<PaymentInsightDto> getPaymentBreakdown(Long storeAdminId) {
        return orderRepository.getSalesByPaymentMethod(storeAdminId);
    }

    @Override
    public BranchPerformanceDto getBranchPerformance(Long storeAdminId) {
        return BranchPerformanceDto.builder()
                .branchSales(orderRepository.getSalesByBranch(storeAdminId))
                .newBranchesThisMonth(branchRepository.countNewBranchesThisMonth(storeAdminId))
//                .topBranch(branchRepository.findTopBranchBySales(storeAdminId))
                .build();
    }

    @Override
    public StoreAlertDto getStoreAlerts(Long storeAdminId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        userRepository.findInactiveCashiers(storeAdminId, sevenDaysAgo);

        return StoreAlertDto.builder()
                .lowStockAlerts(productRepository.findLowStockProducts(storeAdminId))
                .noSalesToday(branchRepository.findBranchesWithNoSalesToday(storeAdminId))
                .refundSpikeAlerts(refundRepository.findRefundSpikes(storeAdminId))
                .inactiveCashiers(userRepository.findInactiveCashiers(storeAdminId, sevenDaysAgo))
                .build();
    }
}
