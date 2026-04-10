package com.zosh.controller;

import com.zosh.exception.UserException;
import com.zosh.mapper.ShiftReportMapper;
import com.zosh.modal.ShiftReport;
import com.zosh.payload.dto.ShiftReportDTO;
import com.zosh.service.ShiftReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shift-reports")
@RequiredArgsConstructor
public class ShiftReportController {

    private final ShiftReportService shiftReportService;
    private final ShiftReportMapper shiftReportMapper;

    /**
     * 🔄 Start a new shift (only once per day)
     */
    @PostMapping("/start")
    public ResponseEntity<ShiftReport> startShift(
            @RequestParam("branchId") Long branchId
    ) throws UserException {
        // current user will be auto-fetched from session in service
        ShiftReport shift = shiftReportService.startShift(
                null,
                branchId,
                LocalDateTime.now());
        return ResponseEntity.ok(shift);
    }

    /**
     * 🛑 End the current shift for logged-in cashier
     */
    @PatchMapping("/end")
    public ResponseEntity<ShiftReportDTO> endShift() throws UserException {
        ShiftReport ended = shiftReportService.endShift(
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(ShiftReportMapper.toDTO(ended));
    }

    /**
     * 📊 Get current shift progress (live data) by cashierId
     */
    @GetMapping("/current")
    public ResponseEntity<ShiftReportDTO> getCurrentShiftProgress(
           ) throws UserException {
        ShiftReport shift = shiftReportService.getCurrentShiftProgress(null);
        return ResponseEntity.ok(ShiftReportMapper.toDTO(shift));
    }

    /**
     * 📅 Get shift report by date (for cashier)
     */
    @GetMapping("/cashier/{cashierId}/by-date")
    public ResponseEntity<ShiftReportDTO> getShiftReportByDate(
            @PathVariable("cashierId") Long cashierId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        ShiftReport shift = shiftReportService.getShiftReportByCashierAndDate(
                cashierId, date);
        
        return ResponseEntity.ok(ShiftReportMapper.toDTO(shift));
    }

    /**
     * 👤 Get all shift reports for a cashier
     */
    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<ShiftReportDTO>> getShiftsByCashier(
            @PathVariable("cashierId") Long cashierId
    ) {
        List<ShiftReport> shift = shiftReportService
                .getShiftReportsByCashier(cashierId);
        List<ShiftReportDTO> dto = shift.stream()
                .map(ShiftReportMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    /**
     * 🏬 Get all shift reports for a branch
     */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ShiftReportDTO>> getShiftsByBranch(
            @PathVariable("branchId") Long branchId
    ) {
        List<ShiftReport> shifts = shiftReportService.getShiftReportsByBranch(branchId);
        List<ShiftReportDTO> dto = shifts.stream()
                .map(ShiftReportMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    /**
     * 📋 Get all shift reports (admin use)
     */
    @GetMapping
    public ResponseEntity<List<ShiftReportDTO>> getAllShifts() {
        List<ShiftReport> shifts=shiftReportService.getAllShiftReports();

        List<ShiftReportDTO> dto = shifts.stream()
                .map(ShiftReportMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    /**
     * 🔍 Get a shift by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShiftReportDTO> getShiftById(@PathVariable("id") Long id) {
        ShiftReport shifts=shiftReportService.getShiftReportById(id);

        return ResponseEntity.ok(ShiftReportMapper.toDTO(shifts));
    }

    /**
     * ❌ Delete a shift report (admin use)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShift(@PathVariable("id") Long id) {
        shiftReportService.deleteShiftReport(id);
        return ResponseEntity.ok().build();
    }
}
