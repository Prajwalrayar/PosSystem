package com.zosh.controller;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.exception.UserException;
import com.zosh.modal.User;
import com.zosh.payload.dto.BranchDTO;
import com.zosh.payload.request.BranchSettingsRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.BranchSettingsResponse;
import com.zosh.payload.dto.UserDTO;
import com.zosh.service.BranchService;
import com.zosh.service.StoreService;
import com.zosh.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;
    private final UserService userService;


    // 🔹 Create Branch
    @PostMapping
    public ResponseEntity<BranchDTO> createBranch(
            @Valid @RequestBody BranchDTO dto,

            @RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(branchService.createBranch(dto,user));
    }

    // 🔹 Get Branch by ID
    @GetMapping("/{id}")
	public ResponseEntity<BranchDTO> getBranch(@PathVariable("id") Long id) {
        return ResponseEntity.ok(branchService.getBranchById(id));
    }

    // 🔹 Get All Branches (No Pagination)
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<BranchDTO>> getAllBranches(
            @RequestHeader("Authorization") String jwt,
			@PathVariable("storeId") Long storeId
    ) throws UserException {
        User user=userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(branchService.getAllBranchesByStoreId(storeId));
    }

    // 🔹 Update Branch
    @PutMapping("/{id}")
    public ResponseEntity<BranchDTO> updateBranch(
			@PathVariable("id") Long id,
            @RequestBody BranchDTO dto,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user=userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(branchService.updateBranch(id, dto, user));
    }

    // 🔹 Delete Branch
    @DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBranch(@PathVariable("id") Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{branchId}/settings")
    public ResponseEntity<ApiResponseBody<BranchSettingsResponse>> updateBranchSettings(
            @PathVariable("branchId") Long branchId,
            @Valid @RequestBody BranchSettingsRequest request
    ) {
        BranchSettingsResponse response = branchService.updateBranchSettings(branchId, request);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Branch settings updated", response));
    }

    @GetMapping("/{branchId}/settings")
    public ResponseEntity<ApiResponseBody<BranchSettingsResponse>> getBranchSettings(
            @PathVariable("branchId") Long branchId
    ) {
        BranchSettingsResponse response = branchService.getBranchSettings(branchId);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Branch settings fetched", response));
    }


}
