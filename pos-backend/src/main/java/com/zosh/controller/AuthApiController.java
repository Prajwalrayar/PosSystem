package com.zosh.controller;

import com.zosh.payload.request.ChangePasswordRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserService userService;

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponseBody<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(new ApiResponseBody<Void>(true, "Password changed successfully", null));
    }
}
