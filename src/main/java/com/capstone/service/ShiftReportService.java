package com.capstone.service;

import com.capstone.payload.dto.ShiftReportDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ShiftReportService {
    /**
     * Start a new shift for the cashier at a specific branch.
     */
    ShiftReportDto startShift() throws Exception;

    /**
     * End the shift and generate full summary report including:
     * - total sales, refunds, net sales
     * - payment breakdown
     * - top selling products
     * - recent orders
     * - refunds processed
     */
    ShiftReportDto endShift(Long shiftReportId, LocalDateTime shiftEnd) throws Exception;

    /**
     * Get a single shift report by ID.
     */
    ShiftReportDto getShiftReportById(Long id) throws Exception;

    /**
     * Get all shift reports.
     */
    List<ShiftReportDto> getAllShiftReports();

    /**
     * Get shift reports for a specific cashier.
     */
    List<ShiftReportDto> getShiftReportsByCashierId(Long cashierId);

    /**
     * Get current shift progress without ending the shift.
     */
    ShiftReportDto getCurrentShiftProgress(Long cashierId) throws Exception;
    /**
     * Get shift reports for a specific branch.
     */
    List<ShiftReportDto> getShiftReportsByBranchId(Long branchId);

    /**
     * Get a cashier's shift report for a specific date.
     */
    ShiftReportDto getShiftReportByCashierAndDate(Long cashierId, LocalDateTime date);


    /**
     * Delete a shift report by ID.
     */
    void deleteShiftReport(Long id);
}
