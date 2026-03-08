package com.capstone.controllers;

import com.capstone.model.Branch;
import com.capstone.payload.dto.BranchDto;
import com.capstone.payload.response.ApiResponse;
import com.capstone.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    public ResponseEntity<BranchDto> createBranch(@RequestBody BranchDto branchDto) throws Exception {
        BranchDto createdBranch = branchService.createBranch(branchDto);
        return ResponseEntity.ok(createdBranch);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchDto> getBranch(@PathVariable Long id) throws Exception {
        BranchDto branch = branchService.getBranchById(id);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<BranchDto>> getAllBranchesByStoreId(@PathVariable Long storeId) throws Exception {
        List<BranchDto> allBranches = branchService.getAllBranchesByStoreId(storeId);
        return ResponseEntity.ok(allBranches);
    }

    @PutMapping("/{id}")
    public ResponseEntity <BranchDto> updateBranch(@PathVariable Long id,
                                                   @RequestBody BranchDto branchDto) throws Exception {
        BranchDto updatedBranch = branchService.updateBranch(id,branchDto);
        return ResponseEntity.ok(updatedBranch);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <ApiResponse> deleteBranch(@PathVariable Long id) throws Exception {
        branchService.deleteBranch(id);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Branch deleted successfully!");
        return ResponseEntity.ok(apiResponse);  
    }
}
