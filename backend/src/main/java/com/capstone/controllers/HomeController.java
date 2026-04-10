package com.capstone.controllers;

import com.capstone.payload.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class HomeController {
    @GetMapping
    public ResponseEntity<ApiResponse> home(){
        ApiResponse response = new ApiResponse();
        response.setMessage("Welcome to Smart POS");
        return ResponseEntity.ok(response);
    }
}
