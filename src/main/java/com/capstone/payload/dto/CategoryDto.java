package com.capstone.payload.dto;

import com.capstone.model.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {
    private Long id;

    private String name;

//    private Store store;

    private Long storeId;
}
