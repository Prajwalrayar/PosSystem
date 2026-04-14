package com.zosh.service.impl;

import com.zosh.ai.ForecastService;
import com.zosh.domain.OrderStatus;
import com.zosh.domain.PaymentType;
import com.zosh.exception.UserException;
import com.zosh.mapper.OrderMapper;
import com.zosh.modal.*;
import com.zosh.payload.dto.OrderDTO;
import com.zosh.repository.*;
import com.zosh.service.OrderService;
import com.zosh.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;

    @Autowired
    private ForecastService forecastService; // 🔥 REAL-TIME SERVICE

    // ✅ CREATE ORDER (WITH REAL-TIME FORECAST)
    @Override
    public OrderDTO createOrder(OrderDTO dto) throws UserException {

        User cashier = userService.getCurrentUser();
        Branch branch = cashier.getBranch();

        if (branch == null) {
            throw new UserException("Cashier's branch is null");
        }

        Store store = branch.getStore();
        Customer customer = resolveOrderCustomer(dto.getCustomer(), store);

        Order order = Order.builder()
                .branch(branch)
                .cashier(cashier)
                .customer(customer)
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

        double total = orderItems.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();

        order.setSubtotal(total);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        // 💾 Save order
        Order savedOrder = orderRepository.save(order);

        // 🔥 REAL-TIME FORECAST TRIGGER (VERY IMPORTANT)
        for (OrderItem item : orderItems) {
            Long productId = item.getProduct().getId();
            // forecastService.update(productId);
        }

        return OrderMapper.toDto(savedOrder);
    }

    // ✅ GET ORDER BY ID
    @Override
    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    // ✅ GET ORDERS WITH FILTERS
    @Override
    public List<OrderDTO> getOrdersByBranch(Long branchId,
                                            Long customerId,
                                            Long cashierId,
                                            PaymentType paymentType,
                                            OrderStatus status) {

        return orderRepository.findByBranchId(branchId).stream()

                .filter(order -> customerId == null ||
                        (order.getCustomer() != null &&
                                order.getCustomer().getId().equals(customerId)))

                .filter(order -> cashierId == null ||
                        (order.getCashier() != null &&
                                order.getCashier().getId().equals(cashierId)))

                .filter(order -> paymentType == null ||
                        order.getPaymentType() == paymentType)

                .map(OrderMapper::toDto)

                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))

                .collect(Collectors.toList());
    }

    // ✅ GET ORDERS BY CASHIER
    @Override
    public List<OrderDTO> getOrdersByCashier(Long cashierId) {
        return orderRepository.findByCashierId(cashierId).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    // ✅ DELETE ORDER
    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order not found");
        }
        orderRepository.deleteById(id);
    }

    // ✅ TODAY ORDERS
    @Override
    public List<OrderDTO> getTodayOrdersByBranch(Long branchId) {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return orderRepository
                .findByBranchIdAndCreatedAtBetween(branchId, start, end)
                .stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    // ✅ GET ORDERS BY CUSTOMER
    @Override
    public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
        Store store = resolveCurrentStore();
        if (store == null) {
            throw new EntityNotFoundException("Store not found");
        }
        Customer customer = customerRepository.findByStore_IdAndId(store.getId(), customerId);
        if (customer == null) {
            throw new EntityNotFoundException("Customer not found");
        }

        List<Order> orders = orderRepository.findByCustomerId(customerId);

        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    // ✅ TOP 5 RECENT ORDERS
    @Override
    public List<OrderDTO> getTop5RecentOrdersByBranchId(Long branchId) {

        branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        List<Order> orders = orderRepository
                .findTop5ByBranchIdOrderByCreatedAtDesc(branchId);

        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    private Store resolveCurrentStore() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getStore() != null) {
            return currentUser.getStore();
        }
        if (currentUser.getBranch() != null && currentUser.getBranch().getStore() != null) {
            return currentUser.getBranch().getStore();
        }
        Store store = storeRepository.findByStoreAdminId(currentUser.getId());
        if (store != null) {
            return store;
        }
        throw new EntityNotFoundException("Store not found");
    }

    private Customer resolveOrderCustomer(Customer requestCustomer, Store store) {
        if (requestCustomer == null) {
            return null;
        }

        if (requestCustomer.getId() != null) {
            Customer existing = customerRepository.findByStore_IdAndId(store.getId(), requestCustomer.getId());
            if (existing == null) {
                throw new EntityNotFoundException("Customer not found");
            }
            return existing;
        }

        Customer resolved = null;
        if (requestCustomer.getEmail() != null && !requestCustomer.getEmail().isBlank()) {
            String email = requestCustomer.getEmail().trim().toLowerCase();
            resolved = customerRepository.findByStore_IdAndEmailIgnoreCase(store.getId(), email);
            if (resolved == null) {
                resolved = new Customer();
            }
            resolved.setEmail(email);
        } else {
            resolved = new Customer();
        }

        resolved.setFullName(requestCustomer.getFullName());
        resolved.setPhone(requestCustomer.getPhone());
        resolved.setStore(store);
        return customerRepository.save(resolved);
    }
}
