package com.capstone.controllers;

import com.capstone.payload.dto.ShiftReportDto;
import com.capstone.service.ShiftReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shift-reports")
public class ShiftReportController {

    private final ShiftReportService shiftReportService;

    @PostMapping("/start")
    public ResponseEntity<ShiftReportDto> startShift() throws Exception {
        return ResponseEntity.ok(shiftReportService.startShift());
    }

    @PatchMapping("/end")
    public ResponseEntity<ShiftReportDto> endShift() throws Exception {
        return ResponseEntity.ok(shiftReportService.endShift(null,null));
    }

    @GetMapping("/current")
    public ResponseEntity<ShiftReportDto> getCurrentShiftProgress() throws Exception {
        return ResponseEntity.ok(shiftReportService.getCurrentShiftProgress(null));
    }

    @GetMapping("/cashier/{cashierId}/by-date")
    public ResponseEntity<ShiftReportDto> getShiftReportByDate(@PathVariable Long cashierId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ){
        return ResponseEntity.ok(shiftReportService.getShiftReportByCashierAndDate(cashierId,date));
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<ShiftReportDto>> getShiftReportsByCashier(
            @PathVariable Long cashierId) throws Exception {
        return ResponseEntity.ok(
                shiftReportService.getShiftReportsByCashierId(cashierId)
        );
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ShiftReportDto>> getShiftReportsByBranch(
            @PathVariable Long branchId
    ) {
        return ResponseEntity.ok(
                shiftReportService.getShiftReportsByBranchId(branchId)
        );
    }

    /**
     * 📋 Get all shift reports (admin use)
     */
//    @GetMapping
//    public ResponseEntity<List<ShiftReportDto>> getAllShifts() {
//        List<ShiftReport> shifts=shiftReportService.getAllShiftReports();
//
//        List<ShiftReportDto> dto = shifts.stream()
//                .map(ShiftReportMapper::toDTO).collect(Collectors.toList());
//        return ResponseEntity.ok(dto);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftReportDto> getShiftById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(
                shiftReportService.getShiftReportById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        shiftReportService.deleteShiftReport(id);
        return ResponseEntity.ok().build();
    }
}
