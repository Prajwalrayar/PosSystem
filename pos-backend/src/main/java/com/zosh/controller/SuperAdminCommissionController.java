package com.zosh.controller;

import com.zosh.exception.ResourceNotFoundException;
import com.zosh.payload.request.CommissionUpdateRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.CommissionResponse;
import com.zosh.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/commissions")
@RequiredArgsConstructor
public class SuperAdminCommissionController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<ApiResponseBody<List<CommissionResponse>>> getCommissions() {
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Commissions fetched", storeService.getCommissionList()));
    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<ApiResponseBody<CommissionResponse>> updateCommission(
            @PathVariable("storeId") Long storeId,
            @Valid @RequestBody CommissionUpdateRequest request
    ) throws ResourceNotFoundException {
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Commission updated", storeService.updateCommission(storeId, request)));
    }
}
