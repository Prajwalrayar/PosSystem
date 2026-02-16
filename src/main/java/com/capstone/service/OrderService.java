package com.capstone.service;

import com.capstone.domain.OrderStatus;
import com.capstone.domain.PaymentType;
import com.capstone.exceptions.UserException;
import com.capstone.payload.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(OrderDto dto) throws Exception;
    OrderDto getOrderById(Long id);

    List<OrderDto> getOrdersByBranch(Long branchId,
                                     Long customerId,
                                     Long cashierId,
                                     PaymentType paymentType,
                                     OrderStatus status);
    List<OrderDto> getOrdersByCashier(Long cashierId);
    void deleteOrder(Long id);
    List<OrderDto> getTodayOrdersByBranch(Long branchId);
    List<OrderDto> getOrdersByCustomerId(Long customerId);
    List<OrderDto> getTop5RecentOrdersByBranchId(Long branchId);
}
