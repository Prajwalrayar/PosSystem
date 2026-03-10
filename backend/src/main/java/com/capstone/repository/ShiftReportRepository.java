package com.capstone.repository;

import com.capstone.model.Branch;
import com.capstone.model.ShiftReport;
import com.capstone.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShiftReportRepository extends JpaRepository<ShiftReport, Long> {

    /**
     * Get all shift reports for a specific cashier.
     */
    List<ShiftReport> findByCashier(Users cashier);
    List<ShiftReport> findByCashierId(Long id);

    /**
     * Get all shift reports for a specific branch.
     */
    List<ShiftReport> findByBranch(Branch branch);
    List<ShiftReport> findByBranchId(Long id);

    /**
     * Get latest open shift for a cashier (where shiftEnd is null).
     */
    Optional<ShiftReport> findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(Users cashier);

    /**
     * Get shift report for a specific date for a cashier.
     */
    Optional<ShiftReport> findByCashierAndShiftStartBetween(Users cashier, LocalDateTime start,
                                                            LocalDateTime end);

}
