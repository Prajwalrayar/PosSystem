package com.capstone.repository;

import com.capstone.model.OrderItem;
import com.capstone.payload.BranchAnalytics.CategorySalesDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
        SELECT p.id, p.name, SUM(oi.quantity) as totalProducts FROM OrderItem oi
        JOIN oi.product p
        JOIN oi.order o
        WHERE o.branch.id = :branchId
        GROUP BY p.id, p.name
        ORDER BY totalProducts DESC
    """)
    List<Object[]> getTopProductsByQuantity(@Param("branchId") Long branchId);


    @Query("""
        SELECT c.name, SUM(oi.quantity * oi.price) as totalAmount
        FROM OrderItem oi
        JOIN oi.product p
        JOIN p.category c
        JOIN oi.order o
        WHERE o.branch.id = :branchId AND o.createdAt BETWEEN :start AND :end
        GROUP BY c.name
        ORDER BY totalAmount DESC
    """)
    List<Object[]> getCategoryWiseSales(
            @Param("branchId") Long branchId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

//    @Query("""
//        SELECT new com.capstone.payload.BranchAnalytics.CategorySalesDto(
//        p.category.name, SUM(oi.quantity * p.sellingPrice),
//        SUM(oi.quantity))
//        FROM OrderItem oi
//        JOIN oi.product p
//        WHERE p.store.storeAdmin.id =:storeAdminId
//        GROUP BY p.category.name
//    """)
//    List<CategorySalesDto> getSalesGroupedByCategory(@Param("storeAdminId")  Long storeAdminId);
}
