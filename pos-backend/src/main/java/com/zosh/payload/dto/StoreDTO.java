package com.zosh.payload.dto;

import com.zosh.domain.StoreStatus;
import com.zosh.modal.StoreContact;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StoreDTO {
    private Long id;
    private String brand;
    private Long storeAdminId;
    private UserDTO storeAdmin;
    private String storeType;
    private StoreStatus status;
    private String description;
    private StoreContact contact;
    private BigDecimal commissionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
