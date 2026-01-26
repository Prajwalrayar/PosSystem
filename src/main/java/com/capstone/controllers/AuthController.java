package com.capstone.controllers;

import com.capstone.exceptions.UserException;
import com.capstone.payload.dto.UserDto;
import com.capstone.payload.response.AuthResponse;
import com.capstone.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUpHandler(@RequestBody UserDto userDto) throws UserException {
        return ResponseEntity.ok(authService.SignUp(userDto));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(@RequestBody UserDto userDto) throws Exception {
        return ResponseEntity.ok(authService.Login(userDto));
    }
}
