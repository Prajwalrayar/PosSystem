package com.zosh.service;

import com.zosh.exception.ResourceNotFoundException;
import com.zosh.payload.request.CompletePaymentRequest;
import com.zosh.payload.response.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse completePaymentAndCreateInvoice(Long orderId, CompletePaymentRequest request) throws Exception;
    InvoiceResponse resendInvoiceEmail(Long invoiceId) throws ResourceNotFoundException;
    InvoiceResponse getInvoiceStatus(Long invoiceId) throws ResourceNotFoundException;
    byte[] getInvoicePdf(Long invoiceId) throws ResourceNotFoundException;
}
