package com.capstone.controllers;

import com.capstone.mapper.RefundMapper;
import com.capstone.model.Refund;
import com.capstone.payload.dto.RefundDto;
import com.capstone.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refunds")
public class RefundController {
    private final RefundService refundService;

    // ✅ 1. Create a refund
    @PostMapping
    public ResponseEntity<RefundDto> createRefund(@RequestBody RefundDto refundDto) throws Exception {
        RefundDto refund = refundService.createRefund(refundDto);
        return ResponseEntity.ok(refund);
    }

    // ✅ 2. Get all refunds (admin)
    @GetMapping
    public ResponseEntity<List<RefundDto>> getAllRefunds() {
        List<RefundDto> refunds = refundService.getAllRefunds();
        return ResponseEntity.ok(refunds);
    }

    // ✅ 3. Get refunds by cashier
    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<RefundDto>> getRefundsByCashier(
            @PathVariable Long cashierId) {
        List<RefundDto> refunds = refundService.getRefundsByCashier(cashierId);
        return ResponseEntity.ok(refunds);
    }

    // ✅ 4. Get refunds by branch
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<RefundDto>> getRefundsByBranch(@PathVariable Long branchId) {
        List<RefundDto> refunds = refundService.getRefundsByBranch(branchId);
        return ResponseEntity.ok(refunds);
    }

    // ✅ 5. Get refunds by shift report
    @GetMapping("/shift/{shiftId}")
    public ResponseEntity<List<RefundDto>> getRefundsByShift(@PathVariable Long shiftId) {
        List<RefundDto> refunds = refundService.getRefundsByShiftReport(shiftId);
        return ResponseEntity.ok(refunds);
    }

    // ✅ 6. Get refunds by cashier and date range
    @GetMapping("/cashier/{cashierId}/range")
    public ResponseEntity<List<RefundDto>> getRefundsByCashierAndDateRange(
            @PathVariable Long cashierId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
    {
        List<RefundDto> refunds = refundService
                .getRefundsByCashierAndDateRange(cashierId, from, to);
        return ResponseEntity.ok(refunds);
    }

    // ✅ 7. Get refund by ID
    @GetMapping("/{id}")
    public ResponseEntity<RefundDto> getRefundById(@PathVariable Long id) throws Exception {
        RefundDto refund = refundService.getRefundById(id);
        return ResponseEntity.ok(refund);
    }

    // ✅ 8. Delete refund
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRefund(@PathVariable Long id) throws Exception {
        refundService.deleteRefund(id);
        return ResponseEntity.ok("Refund deleted successfully.");
    }
}
