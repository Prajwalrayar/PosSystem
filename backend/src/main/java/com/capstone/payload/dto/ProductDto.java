package com.capstone.payload.dto;

import com.capstone.model.Store;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {

    private Long  id;

    private String name;

    private String sku;

    private String description;

    private Double mrp;

    private Double sellingPrice;
    private String brand;
    private String image;

    private CategoryDto category;

    private Long categoryId;

    private Long storeId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
