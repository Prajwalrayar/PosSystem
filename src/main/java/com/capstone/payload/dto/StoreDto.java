package com.capstone.payload.dto;

import com.capstone.domain.StoreStatus;
import com.capstone.model.StoreContact;
import com.capstone.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreDto {
    private Long id;

    private String brand;

    private UserDto storeAdmin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String description;

    private String StoreType;

    private StoreStatus status;

    private StoreContact contact;
}
