package com.zosh.controller;

import com.zosh.exception.ResourceNotFoundException;
import com.zosh.payload.response.ApiResponseBody;
import com.zosh.payload.response.InvoiceResponse;
import com.zosh.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/{invoiceId}/send-email")
    @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_CASHIER', 'ROLE_CASHIER', 'ROLE_BRANCH_MANAGER', 'ROLE_BRANCH_ADMIN')")
    public ResponseEntity<ApiResponseBody<InvoiceResponse>> resendInvoiceEmail(@PathVariable Long invoiceId) throws ResourceNotFoundException {
        InvoiceResponse response = invoiceService.resendInvoiceEmail(invoiceId);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Invoice email queued", response));
    }

    @GetMapping("/{invoiceId}/status")
    public ResponseEntity<ApiResponseBody<InvoiceResponse>> getInvoiceStatus(@PathVariable Long invoiceId) throws ResourceNotFoundException {
        InvoiceResponse response = invoiceService.getInvoiceStatus(invoiceId);
        return ResponseEntity.ok(new ApiResponseBody<>(true, "Invoice status fetched", response));
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long invoiceId) throws ResourceNotFoundException {
        byte[] pdf = invoiceService.getInvoicePdf(invoiceId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"invoice-" + invoiceId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
