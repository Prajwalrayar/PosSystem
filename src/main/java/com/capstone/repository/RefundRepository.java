package com.capstone.repository;

import com.capstone.model.Refund;
import com.capstone.model.Users;
import com.capstone.payload.dto.RefundDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByCashierIdAndCreatedAtBetween(Long cashierId, LocalDateTime from, LocalDateTime to);

    List<Refund> findByCashierId(Long cashierId);

    List<Refund> findByShiftReportId(Long shiftReportId);

    List<Refund> findByBranchId(Long branchId);

    //    store analysis
//    @Query("SELECT COUNT(r) FROM Refund r WHERE r.order.branch.store.storeAdmin.id = :storeAdminId")
//    int countByStoreAdminId(@Param("storeAdminId") Long storeAdminId);
//
//    @Query("""
//          SELECT new com.zosh.payload.dto.RefundDTO(
//                  r.id,
//                  r.order.id,
//                  r.reason,
//                  r.amount,
//                  r.order.cashier.fullName,
//                  r.shiftReport.id,
//                  r.order.branch.id,
//                  r.createdAt
//              )
//            FROM Refund r
//            WHERE r.order.branch.store.storeAdmin.id = :storeAdminId
//            GROUP BY FUNCTION('DATE', r.createdAt),\s
//                         r.id, r.order.id, r.reason, r.amount,\s
//                         r.order.cashier.fullName, r.shiftReport.id,\s
//                         r.order.branch.id, r.createdAt
//            HAVING SUM(r.amount) > 5000
//    """)
//    List<RefundDto > findRefundSpikes(@Param("storeAdminId") Long storeAdminId);
}
