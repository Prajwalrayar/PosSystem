package com.capstone.payload.dto;

import com.capstone.model.Order;
import com.capstone.model.Product;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDto {
    private Long id;

    private Integer quantity;

    private Double price;

    private ProductDto product;

    private Long productId;

    private Long orderId;
}
