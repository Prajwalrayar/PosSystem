package com.zosh.controller;

import com.zosh.exception.ResourceNotFoundException;
import com.zosh.payload.request.InitiateReturnRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.ReturnInitiationResponse;
import com.zosh.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponseBody<ReturnInitiationResponse>> initiateReturn(
            @Valid @RequestBody InitiateReturnRequest request
    ) throws ResourceNotFoundException {
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Return session created", returnService.initiateReturn(request)));
    }
}
