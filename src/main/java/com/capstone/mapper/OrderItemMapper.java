package com.capstone.mapper;

import com.capstone.model.OrderItem;
import com.capstone.payload.dto.OrderItemDto;

public class OrderItemMapper {
    public static OrderItemDto toDto(OrderItem item) {
        if (item == null) return null;

        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .product(item.getProduct() != null ? ProductMapper.toDto(item.getProduct()) : null)
                .build();
    }
}
