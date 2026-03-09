package com.capstone.service.impl;

import com.capstone.domain.OrderStatus;
import com.capstone.domain.PaymentType;
import com.capstone.exceptions.UserException;
import com.capstone.mapper.OrderMapper;
import com.capstone.model.Branch;
import com.capstone.model.Order;
import com.capstone.model.Product;
import com.capstone.model.Users;
import com.capstone.payload.dto.OrderDto;
import com.capstone.model.OrderItem;
import com.capstone.repository.BranchRepository;
import com.capstone.repository.OrderRepository;
import com.capstone.repository.ProductRepository;
import com.capstone.service.OrderService;
import com.capstone.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final UserService userService;

    @Override
    public OrderDto createOrder(OrderDto dto) throws Exception {
        Users cashier = userService.getCurrentUser();

        Branch branch=cashier.getBranch();

        if(branch==null){
            throw new UserException("cashier's branch is null");
        }

        Order order = Order.builder()
                .branch(branch)
                .cashier(cashier)
                .customer(dto.getCustomer())
                .paymentType(dto.getPaymentType())
                .build();

        List<OrderItem> orderItems = dto.getItems().stream().map(itemDto -> {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            return OrderItem.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .price(product.getSellingPrice() * itemDto.getQuantity())
                    .order(order)

                    .build();
        }).toList();

        double total = orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
        order.setTotalAmount(total);
        order.setItems(orderItems);

        return OrderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    @Override
    public List<OrderDto> getOrdersByBranch(Long branchId,
                                            Long customerId,
                                            Long cashierId,
                                            PaymentType paymentType,
                                            OrderStatus status) {
        return orderRepository.findByBranchId(branchId).stream()
                .filter(order -> customerId == null ||
                        (order.getCustomer() != null &&
                                order.getCustomer().getId().equals(customerId)))

                .filter(order -> cashierId==null ||
                        (order.getCashier() != null &&
                                order.getCashier().getId().equals(cashierId)))

                .filter(order -> paymentType == null ||
                        order.getPaymentType() == paymentType)

//                .filter(order -> status() == null ||
//                        order.getStatus() == status)

                .map(OrderMapper::toDto)

//                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))

                .collect(Collectors.toList());
//        return orderRepository.findByBranchId(branchId).stream()
//                .map(OrderMapper::toDto)
//                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByCashier(Long cashierId) {
        return orderRepository.findByCashierId(cashierId).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order not found");
        }
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderDto> getTodayOrdersByBranch(Long branchId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return orderRepository.findByBranchIdAndCreatedAtBetween(branchId, start, end)
                .stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);

        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getTop5RecentOrdersByBranchId(Long branchId) {
//        branchRepository.findById(branchId)
//                .orElseThrow(() -> new EntityNotFoundException("Branch not found with ID: " + branchId));

        List<Order> orders = orderRepository.findTop5ByBranchIdOrderByCreatedAtDesc(branchId);
        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }
}
