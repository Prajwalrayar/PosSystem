package com.zosh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.domain.OrderStatus;
import com.zosh.exception.BusinessValidationException;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.mapper.OrderItemMapper;
import com.zosh.modal.Order;
import com.zosh.modal.ReturnSession;
import com.zosh.modal.User;
import com.zosh.payload.dto.OrderItemDTO;
import com.zosh.payload.request.InitiateReturnRequest;
import com.zosh.payload.response.ReturnInitiationResponse;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.ReturnSessionRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.ReturnService;
import com.zosh.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private final ReturnSessionRepository returnSessionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ReturnInitiationResponse initiateReturn(InitiateReturnRequest request) throws ResourceNotFoundException {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.getId().equals(request.getCashierId())) {
            throw new AccessDeniedException("You can initiate returns only for your own cashier session");
        }
        if (currentUser.getBranch() == null || !currentUser.getBranch().getId().equals(request.getBranchId())) {
            throw new AccessDeniedException("You can initiate returns only for your branch");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getBranch() == null || !order.getBranch().getId().equals(request.getBranchId())) {
            throw new BusinessValidationException("branchId", "Order does not belong to the provided branch");
        }
        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessValidationException("orderId", "Order is already refunded");
        }

        List<OrderItemDTO> eligibleItems = order.getItems().stream()
                .map(OrderItemMapper::toDto)
                .toList();

        ReturnSession session = new ReturnSession();
        session.setOrder(order);
        session.setCashier(userRepository.findById(request.getCashierId())
                .orElseThrow(() -> new ResourceNotFoundException("Cashier not found")));
        session.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found")));
        session.setEligibleItemsJson(writeJson(eligibleItems));
        ReturnSession saved = returnSessionRepository.save(session);

        return new ReturnInitiationResponse(saved.getId(), eligibleItems);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize return session", ex);
        }
    }
}
