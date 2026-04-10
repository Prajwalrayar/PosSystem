package com.zosh.repository;



import com.zosh.modal.ShiftReport;
import com.zosh.modal.User;
import com.zosh.modal.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShiftReportRepository extends JpaRepository<ShiftReport, Long> {

    /**
     * Get all shift reports for a specific cashier.
     */
    List<ShiftReport> findByCashier(User cashier);

    /**
     * Get all shift reports for a specific branch.
     */
    List<ShiftReport> findByBranch(Branch branch);

    @Modifying
    @Query("UPDATE ShiftReport s SET s.cashier = null WHERE s.cashier.id = :cashierId")
    int clearCashierById(Long cashierId);

    /**
     * Get latest open shift for a cashier (where shiftEnd is null).
     */
    Optional<ShiftReport> findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(User cashier);

    /**
     * Get shift report for a specific date for a cashier.
     */
    Optional<ShiftReport> findByCashierAndShiftStartBetween(User cashier, LocalDateTime start, LocalDateTime end);
}
