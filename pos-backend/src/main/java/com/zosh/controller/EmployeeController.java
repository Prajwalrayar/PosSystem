package com.zosh.controller;

import com.zosh.domain.UserRole;
import com.zosh.modal.User;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.ResetEmployeePasswordRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.EmployeePerformanceResponse;
import com.zosh.payload.response.EmployeePasswordResetResponse;
import com.zosh.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN', 'ROLE_STORE_MANAGER')")
    public ResponseEntity<UserDTO> createStoreEmployee(
            @RequestBody UserDTO employee, @PathVariable("storeId") Long storeId) throws Exception {
        UserDTO createdEmployee = employeeService.createStoreEmployee(employee, storeId);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @PostMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('ROLE_BRANCH_ADMIN', 'ROLE_BRANCH_MANAGER')")
    public ResponseEntity<User> createBranchEmployee(@RequestBody User employee, @PathVariable("branchId") Long branchId) throws Exception {
        User createdEmployee = employeeService.createBranchEmployee(employee, branchId);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN', 'ROLE_STORE_MANAGER', 'ROLE_BRANCH_ADMIN', 'ROLE_BRANCH_MANAGER')")
    public ResponseEntity<User> updateEmployee(@PathVariable("employeeId") Long employeeId, @RequestBody User employeeDetails) throws Exception {
        User updatedEmployee = employeeService.updateEmployee(employeeId, employeeDetails);
        return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN', 'ROLE_BRANCH_ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("employeeId") Long employeeId) throws Exception {
        employeeService.deleteEmployee(employeeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN', 'ROLE_STORE_MANAGER', 'ROLE_BRANCH_ADMIN', 'ROLE_BRANCH_MANAGER')")
    public ResponseEntity<User> findEmployeeById(@PathVariable("employeeId") Long employeeId) throws Exception {
        User employee = employeeService.findEmployeeById(employeeId);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN', 'ROLE_STORE_MANAGER')")
    public ResponseEntity<List<User>> findStoreEmployees(@PathVariable("storeId") Long storeId) throws Exception {
        List<User> employees = employeeService.findStoreEmployees(storeId, null);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('ROLE_BRANCH_ADMIN', 'ROLE_BRANCH_MANAGER')")
    public ResponseEntity<List<User>> findBranchEmployees(
            @PathVariable("branchId") Long branchId,
            @RequestParam(required = false) UserRole role
    ) throws Exception {
        List<User> employees = employeeService.findBranchEmployees(branchId,role);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @PatchMapping("/{employeeId}/reset-password")
    public ResponseEntity<ApiResponseBody<EmployeePasswordResetResponse>> resetEmployeePassword(
            @PathVariable("employeeId") Long employeeId,
            @Valid @RequestBody ResetEmployeePasswordRequest request
    ) throws Exception {
        EmployeePasswordResetResponse response = employeeService.resetEmployeePassword(employeeId, request);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Password reset initiated", response));
    }

    @GetMapping("/{employeeId}/performance")
    public ResponseEntity<ApiResponseBody<EmployeePerformanceResponse>> getEmployeePerformance(
            @PathVariable("employeeId") Long employeeId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) throws Exception {
        EmployeePerformanceResponse response = employeeService.getEmployeePerformance(employeeId, from, to);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Employee performance fetched", response));
    }
}
