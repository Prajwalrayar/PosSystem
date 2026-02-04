package com.capstone.repository;

import com.capstone.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Inventory findByProductIdAndBranchId(Long productId,Long branchId);
    List<Inventory> findByBranchId(Long branchId);
}
