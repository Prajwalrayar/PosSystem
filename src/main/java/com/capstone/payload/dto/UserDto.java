package com.capstone.payload.dto;

import com.capstone.domain.UserRole;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {

    private Long id;


    private String fullName;

    private String email;

    private String password;

    private String phone;

    private UserRole role;

    private Long branchId;
    private Long storeId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
