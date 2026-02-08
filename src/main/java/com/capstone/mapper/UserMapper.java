package com.capstone.mapper;

import com.capstone.model.Users;
import com.capstone.payload.dto.UserDto;
import com.capstone.payload.response.AuthResponse;

import java.time.LocalDateTime;

public class UserMapper {

    public static UserDto toDTO(Users savedUser) {
        UserDto userDto = new UserDto();


        userDto.setId(savedUser.getId());
        userDto.setFullName(savedUser.getFullName());
        userDto.setEmail(savedUser.getEmail());
        userDto.setRole(savedUser.getRole());
        userDto.setCreatedAt(savedUser.getCreatedAt());
        userDto.setUpdatedAt(savedUser.getUpdatedAt());
        userDto.setLastLogin(savedUser.getLastLogin());
        userDto.setPhone(savedUser.getPhone());
        userDto.setBranchId(savedUser.getBranch()!=null? savedUser.getBranch().getId():null);
        userDto.setStoreId(savedUser.getStore()!=null? savedUser.getStore().getId():null);


        return userDto;
    }

    public static Users toEntity(UserDto userDto) {
        Users createdUser = new Users();
        createdUser.setEmail(userDto.getEmail());
        createdUser.setFullName(userDto.getFullName());
        createdUser.setRole(userDto.getRole());
        createdUser.setCreatedAt(LocalDateTime.now());
        createdUser.setUpdatedAt(LocalDateTime.now());
        createdUser.setLastLogin(LocalDateTime.now());
        createdUser.setPhone(userDto.getPhone());
        createdUser.setPassword(userDto.getPassword());
        return createdUser;
    }
}
