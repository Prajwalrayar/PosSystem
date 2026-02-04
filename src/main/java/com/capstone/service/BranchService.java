package com.capstone.service;

import com.capstone.model.Users;
import com.capstone.payload.dto.BranchDto;

import java.util.List;

public interface BranchService {
    BranchDto createBranch(BranchDto branchDto) throws Exception;
    BranchDto updateBranch(Long id, BranchDto branchDto) throws Exception;
    void deleteBranch(Long id) throws Exception;
    List<BranchDto> getAllBranchesByStoreId(Long storeId);

    BranchDto getBranchById(Long id) throws Exception;
}
