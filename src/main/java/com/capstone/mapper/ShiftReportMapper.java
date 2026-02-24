package com.capstone.mapper;

import com.capstone.model.Order;
import com.capstone.model.Product;
import com.capstone.model.Refund;
import com.capstone.model.ShiftReport;
import com.capstone.payload.dto.OrderDto;
import com.capstone.payload.dto.ProductDto;
import com.capstone.payload.dto.RefundDto;
import com.capstone.payload.dto.ShiftReportDto;

import java.util.List;
import java.util.stream.Collectors;

public class ShiftReportMapper {
    public static ShiftReportDto toDTO(ShiftReport entity) {

        return ShiftReportDto.builder()
                .id(entity.getId())
                .shiftStart(entity.getShiftStart())
                .shiftEnd(entity.getShiftEnd())
                .totalSales(entity.getTotalSales())
                .totalOrders(entity.getTotalOrders())
                .netSales(entity.getNetSales())
                .totalRefunds(entity.getTotalRefunds())
                .cashier(UserMapper.toDTO(entity.getCashier()))
                .cashierId(entity.getCashier().getId())
                .branchId(entity.getBranch().getId())
                .recentOrders(mapOrders(entity.getRecentOrders()))
                .topSellingProducts(mapProducts(entity.getTopSellingProducts()))
                .refunds(mapRefunds(entity.getRefunds()))
                .paymentSummaries(entity.getPaymentSummaries())
                .build();

    }

    private static List<OrderDto> mapOrders(List<Order> recentOrders) {
        if (recentOrders == null || recentOrders.isEmpty()) {return null;}
        return recentOrders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    private static List<ProductDto> mapProducts(List<Product> topSellingProducts) {
        if (topSellingProducts == null || topSellingProducts.isEmpty()) {return null;}
        return topSellingProducts.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    private static List<RefundDto> mapRefunds(List<Refund> refunds) {
        if (refunds == null || refunds.isEmpty()) {return null;}
        return refunds.stream()
                .map(RefundMapper::toDTO)
                .collect(Collectors.toList());
    }
}
