package com.capstone.service;

import com.capstone.exceptions.UserException;
import com.capstone.payload.dto.UserDto;
import com.capstone.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse SignUp(UserDto userDto) throws UserException;
    AuthResponse Login(UserDto userDto) throws Exception;
}
