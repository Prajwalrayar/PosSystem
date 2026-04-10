package com.zosh.controller;

import com.zosh.exception.ResourceNotFoundException;
import com.zosh.modal.Customer;
import com.zosh.payload.request.LoyaltyTransactionRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.LoyaltyTransactionResponse;
import com.zosh.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> create(
            @RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.createCustomer(customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(
			@PathVariable("id") Long id,
            @RequestBody Customer customer
    ) throws ResourceNotFoundException {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
			@PathVariable("id") Long id
    ) throws ResourceNotFoundException {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok("Customer deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(
			@PathVariable("id") Long id
    ) throws ResourceNotFoundException {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @PostMapping("/{customerId}/loyalty/transactions")
    public ResponseEntity<ApiResponseBody<LoyaltyTransactionResponse>> createLoyaltyTransaction(
			@PathVariable("customerId") Long customerId,
            @Valid @RequestBody LoyaltyTransactionRequest request
    ) throws ResourceNotFoundException {
        LoyaltyTransactionResponse response = customerService.createLoyaltyTransaction(customerId, request);
        String message = request.getType().name().equals("ADD") ? "Points added" : "Points deducted";
        return ResponseEntity.ok(new ApiResponseBody<>(true, message, response));
    }


}
