package com.zosh.repository;

import com.zosh.modal.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // ===========================
    // 📊 TOP PRODUCTS (QUANTITY)
    // ===========================
    @Query("""
        SELECT p.id, p.name, SUM(oi.quantity)
        FROM OrderItem oi
        JOIN oi.product p
        JOIN oi.order o
        WHERE o.branch.id = :branchId
        GROUP BY p.id, p.name
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<Object[]> getTopProductsByQuantity(@Param("branchId") Long branchId);

    // ===========================
    // 📊 CATEGORY-WISE SALES
    // ===========================
    @Query("""
        SELECT c.name, SUM(oi.quantity * oi.price), SUM(oi.quantity)
        FROM OrderItem oi
        JOIN oi.product p
        JOIN p.category c
        JOIN oi.order o
        WHERE o.branch.id = :branchId 
          AND o.createdAt BETWEEN :start AND :end
        GROUP BY c.name
        ORDER BY SUM(oi.quantity * oi.price) DESC
    """)
    List<Object[]> getCategoryWiseSales(
            @Param("branchId") Long branchId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // =========================================
    // 🔥 REAL-TIME DEMAND FORECASTING METHODS
    // =========================================

    // ✅ Today's sales
    @Query(value = """
        SELECT COALESCE(SUM(quantity),0)
        FROM order_items
        WHERE product_id = :pid
        AND DATE(created_at) = CURDATE()
    """, nativeQuery = true)
    int today(@Param("pid") Long pid);

    // ✅ Yesterday's sales
    @Query(value = """
        SELECT COALESCE(SUM(quantity),0)
        FROM order_items
        WHERE product_id = :pid
        AND DATE(created_at) = CURDATE() - INTERVAL 1 DAY
    """, nativeQuery = true)
    int yesterday(@Param("pid") Long pid);

    // ✅ Last 7 days average
    @Query(value = """
        SELECT COALESCE(AVG(q),0)
        FROM (
            SELECT SUM(quantity) q
            FROM order_items
            WHERE product_id = :pid
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at) DESC
            LIMIT 7
        ) t
    """, nativeQuery = true)
    double avg7(@Param("pid") Long pid);

    // ✅ Last 30 days average
    @Query(value = """
        SELECT COALESCE(AVG(q),0)
        FROM (
            SELECT SUM(quantity) q
            FROM order_items
            WHERE product_id = :pid
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at) DESC
            LIMIT 30
        ) t
    """, nativeQuery = true)
    double avg30(@Param("pid") Long pid);

    // =========================================
    // 📈 GRAPH DATA (ACTUAL SALES)
    // =========================================
    @Query(value = """
        SELECT DATE(created_at) as date, SUM(quantity) as value
        FROM order_items
        WHERE product_id = :pid
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Map<String, Object>> getDemandData(@Param("pid") Long pid);
}