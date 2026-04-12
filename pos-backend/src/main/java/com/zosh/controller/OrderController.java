package com.zosh.controller;

import com.zosh.domain.OrderStatus;
import com.zosh.domain.PaymentType;
import com.zosh.exception.UserException;
import com.zosh.payload.dto.OrderDTO;
import com.zosh.payload.request.CompletePaymentRequest;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.InvoiceResponse;
import com.zosh.service.OrderService;
import com.zosh.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CASHIER')")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO dto) throws UserException {
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }


    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByBranch(
            @PathVariable("branchId") Long branchId,
            @RequestParam(name = "customerId", required = false) Long customerId,
            @RequestParam(name = "cashierId", required = false) Long cashierId,
            @RequestParam(name = "paymentType", required = false) PaymentType paymentType,
            @RequestParam(name = "status", required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByBranch(
                    branchId,
                    customerId,
                    cashierId,
                    paymentType,
                    status
                )
        );
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByCashier(@PathVariable("cashierId") Long cashierId) {
        return ResponseEntity.ok(orderService.getOrdersByCashier(cashierId));
    }

    @GetMapping("/today/branch/{branchId}")
    public ResponseEntity<List<OrderDTO>> getTodayOrders(@PathVariable("branchId") Long branchId) {
        return ResponseEntity.ok(orderService.getTodayOrdersByBranch(branchId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDTO>> getCustomerOrders(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/recent/{branchId}")
    @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_MANAGER', 'ROLE_BRANCH_ADMIN')")
    public ResponseEntity<List<OrderDTO>> getRecentOrders(@PathVariable("branchId") Long branchId) {
        List<OrderDTO> recentOrders = orderService.getTop5RecentOrdersByBranchId(branchId);
        return ResponseEntity.ok(recentOrders);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_STORE_MANAGER') or hasAuthority('ROLE_STORE_ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete-payment")
    @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_CASHIER', 'ROLE_CASHIER', 'ROLE_BRANCH_MANAGER', 'ROLE_BRANCH_ADMIN')")
    public ResponseEntity<ApiResponseBody<InvoiceResponse>> completePayment(
            @PathVariable("id") Long orderId,
            @RequestBody CompletePaymentRequest request
    ) throws Exception {
        InvoiceResponse response = invoiceService.completePaymentAndCreateInvoice(orderId, request);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Invoice created successfully", response));
    }


}

