package com.capstone.repository;

import com.capstone.model.Order;
import com.capstone.model.Users;
import com.capstone.payload.StoreAnalysis.BranchSalesDto;
import com.capstone.payload.StoreAnalysis.PaymentInsightDto;
import com.capstone.payload.StoreAnalysis.TimeSeriesPointDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);
    List<Order> findByBranchId(Long branchId);
    List<Order> findByCashierId(Long cashierId);
    List<Order> findByBranchIdAndCreatedAtBetween(Long branchId, LocalDateTime from, LocalDateTime to);
    List<Order> findByCashierIdAndCreatedAtBetween(Users cashier, LocalDateTime from, LocalDateTime to);
    List<Order> findTop5ByBranchIdOrderByCreatedAtDesc(Long branchId);

    // ** Analytics **

    @Query(""" 
            SELECT SUM(o.totalAmount) 
            FROM Order o 
            WHERE o.branch.id = :branchId  
            AND o.createdAt BETWEEN :start AND :end
           """)
    Optional<BigDecimal> getTotalSalesBetween(@Param("branchId") Long branchId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);


    @Query("""
        SELECT u.id, u.fullName, SUM(o.totalAmount) AS totalRevenue
        FROM Order o
        JOIN o.cashier u
        WHERE o.branch.id = :branchId
        GROUP BY u.id, u.fullName
        ORDER BY totalRevenue DESC
    """)
    List<Object[]> getTopCashiersByRevenue(@Param("branchId") Long branchId);


//    @Query("""
//        SELECT c.name, SUM(oi.quantity * oi.price), SUM(oi.quantity)
//        FROM OrderItem oi
//        JOIN oi.product p
//        JOIN p.category c
//        JOIN oi.order o
//        WHERE o.branch.id = :branchId AND o.createdAt BETWEEN :start AND :end
//        GROUP BY c.name
//        ORDER BY SUM(oi.quantity * oi.price) DESC
//    """)
//    List<Object[]> getCategoryWiseSales(
//            @Param("branchId") Long branchId,
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end
//    );


    @Query("""
        SELECT COUNT(o) FROM Order o
        WHERE o.branch.id = :branchId
        AND DATE(o.createdAt) = :date
    """)
    int countOrdersByBranchAndDate(@Param("branchId") Long branchId,
                                   @Param("date") LocalDate date);


    @Query("""
        SELECT COUNT(DISTINCT o.cashier.id) FROM Order o
        WHERE o.branch.id = :branchId
        AND DATE(o.createdAt) = :date
    """)
    int countDistinctCashiersByBranchAndDate(@Param("branchId") Long branchId,
                                             @Param("date") LocalDate date);

    @Query("""
    SELECT o.paymentType, SUM(o.totalAmount), COUNT(o) FROM Order o
    WHERE o.branch.id = :branchId
    AND DATE(o.createdAt) = :date
    GROUP BY o.paymentType
""")
    List<Object[]> getPaymentBreakdownByMethod(
            @Param("branchId") Long branchId,
            @Param("date") LocalDate date
    );

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.branch.store.storeAdmin.id = :storeAdminId")
    Optional<Double> sumTotalSalesByStoreAdmin(@Param("storeAdminId") Long storeAdminId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.branch.store.storeAdmin.id = :storeAdminId")
    int countByStoreAdminId(@Param("storeAdminId") Long storeAdminId);
//

    @Query("""
    SELECT o FROM Order o 
    WHERE o.branch.store.storeAdmin.id = :storeAdminId 
    AND o.createdAt BETWEEN :start AND :end
""")
    List<Order> findAllByStoreAdminAndCreatedAtBetween(@Param("storeAdminId") Long storeAdminId,
                                                       @Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);



    @Query("""
    SELECT new com.capstone.payload.StoreAnalysis.TimeSeriesPointDto(
        o.createdAt, SUM(o.totalAmount)
    )
    FROM Order o
    WHERE o.branch.store.storeAdmin.id = :storeAdminId
    AND o.createdAt BETWEEN :start AND :end
    GROUP BY o.createdAt
    ORDER BY o.createdAt
""")
    List<TimeSeriesPointDto> getDailySales(@Param("storeAdminId") Long storeAdminId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);


    @Query("""
        SELECT new com.capstone.payload.StoreAnalysis.PaymentInsightDto(
            o.paymentType, SUM(o.totalAmount)
        )
        FROM Order o
        WHERE o.branch.store.storeAdmin.id = :storeAdminId
        GROUP BY o.paymentType
    """)
    List<PaymentInsightDto> getSalesByPaymentMethod(@Param("storeAdminId") Long storeAdminId);


//    @Query("""
//        SELECT new com.capstone.payload.StoreAnalysis.BranchSalesDTO(
//            o.branch.name,
//            SUM(o.totalAmount)
//        )
//        FROM Order o
//        WHERE o.branch.store.storeAdmin.id = :storeAdminId
//        GROUP BY o.branch.id
//    """)
//    List<BranchSalesDto> getSalesByBranch(@Param("storeAdminId") Long storeAdminId);

}
