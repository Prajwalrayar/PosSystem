package com.capstone.service;

import com.capstone.model.Refund;
import com.capstone.payload.dto.RefundDto;

import java.time.LocalDateTime;
import java.util.List;

public interface RefundService {
    RefundDto createRefund(RefundDto refund) throws Exception;

    List<RefundDto> getAllRefunds();

    List<RefundDto> getRefundsByCashier(Long cashierId);

    List<RefundDto> getRefundsByShiftReport(Long shiftReportId);

    List<RefundDto> getRefundsByCashierAndDateRange(Long cashierId,
                                                 LocalDateTime from,
                                                 LocalDateTime to
    );

    List<RefundDto> getRefundsByBranch(Long branchId);

    RefundDto getRefundById(Long refundId) throws Exception;

    void deleteRefund(Long refundId) throws Exception;
}
