package com.capstone.repository;

import com.capstone.model.Branch;
import com.capstone.payload.dto.BranchDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByStoreId(Long storeId);

    @Query("SELECT COUNT(b) FROM Branch b WHERE b.store.storeAdmin.id = :storeAdminId")
    int countByStoreAdminId(@Param("storeAdminId") Long storeAdminId);

    @Query("""
        SELECT COUNT(b)
        FROM Branch b
        WHERE b.store.storeAdmin.id = :storeAdminId
        AND MONTH(b.createdAt) = MONTH(CURRENT_DATE)
    """)
    int countNewBranchesThisMonth(@Param("storeAdminId") Long storeAdminId);

    @Query("""
        SELECT b.name
        FROM Branch b
        JOIN Order o ON o.branch.id = b.id
        WHERE b.store.storeAdmin.id = :storeAdminId
        GROUP BY b.id
        ORDER BY SUM(o.totalAmount) DESC
    """)
    List<String> findTopBranchBySales(@Param("storeAdminId") Long storeAdminId);

    @Query("""
        SELECT new com.zosh.payload.dto.BranchDTO(
        b.id, b.name, b.address
        )
        FROM Branch b
        WHERE b.store.storeAdmin.id = :storeAdminId
        AND b.id NOT IN (
            SELECT DISTINCT o.branch.id
            FROM Order o
            WHERE DATE(o.createdAt) = CURRENT_DATE
        )
    """)
    List<BranchDto> findBranchesWithNoSalesToday(@Param("storeAdminId") Long storeAdminId);
}
