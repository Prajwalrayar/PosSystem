package com.capstone.payload.response;

import com.capstone.payload.dto.UserDto;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class AuthResponse {

    private String jwt;

    private String message;
    private UserDto user;
}
