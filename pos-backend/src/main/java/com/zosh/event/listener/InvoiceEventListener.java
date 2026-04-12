package com.zosh.event.listener;

import com.zosh.domain.InvoiceDeliveryStatus;
import com.zosh.event.InvoiceEmailRequestedEvent;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.modal.Invoice;
import com.zosh.repository.InvoiceRepository;
import com.zosh.service.EmailService;
import com.zosh.service.InvoiceDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventListener {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDocumentService invoiceDocumentService;
    private final EmailService emailService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvoiceEmailRequested(InvoiceEmailRequestedEvent event) {
        try {
            Invoice invoice = invoiceRepository.findDetailedById(event.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + event.getInvoiceId()));

            if (invoice.getCustomerEmail() == null || invoice.getCustomerEmail().isBlank()) {
                invoice.setDeliveryStatus(InvoiceDeliveryStatus.FAILED);
                invoice.setLastError("Customer email is missing");
                invoiceRepository.save(invoice);
                return;
            }

            if (invoice.getPdfContent() == null || invoice.getPdfContent().length == 0) {
                invoice.setPdfContent(invoiceDocumentService.generatePdf(invoice));
                invoice.setPdfGeneratedAt(LocalDateTime.now());
                invoiceRepository.save(invoice);
            }

            String subject = "Invoice " + invoice.getInvoiceNumber() + " from SmartPos";
            String plainText = invoiceDocumentService.buildPlainText(invoice);
            String html = invoiceDocumentService.buildHtml(invoice);
            String fileName = invoice.getInvoiceNumber() + ".pdf";

            Exception lastError = null;
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    emailService.sendEmail(
                            invoice.getCustomerEmail(),
                            subject,
                            plainText,
                            html,
                            fileName,
                            invoice.getPdfContent()
                    );

                    invoice.setDeliveryStatus(InvoiceDeliveryStatus.SENT);
                    invoice.setEmailSentAt(LocalDateTime.now());
                    invoice.setRetryCount(attempt);
                    invoice.setLastError(null);
                    invoiceRepository.save(invoice);
                    return;
                } catch (Exception ex) {
                    lastError = ex;
                    log.warn("Failed to send invoice email on attempt {} for invoice {}: {}",
                            attempt, invoice.getInvoiceNumber(), ex.getMessage());
                }
            }

            invoice.setDeliveryStatus(InvoiceDeliveryStatus.FAILED);
            invoice.setRetryCount(3);
            invoice.setLastError(lastError != null ? lastError.getMessage() : "Failed to send invoice email");
            invoiceRepository.save(invoice);
        } catch (Exception ex) {
            log.error("Invoice email processing failed for invoice {}: {}", event.getInvoiceId(), ex.getMessage(), ex);
        }
    }
}
