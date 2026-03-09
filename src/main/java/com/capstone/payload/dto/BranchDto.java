package com.capstone.payload.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class BranchDto {

    private Long id;

    private String name;

    private String address;

    private String phone;

    private String email;

    private List<String> workingDays;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private StoreDto store;

    private Long storeId;

    private UserDto manager;
}
