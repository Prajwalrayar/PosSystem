package com.zosh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.domain.InvoiceDeliveryStatus;
import com.zosh.domain.PaymentType;
import com.zosh.domain.UserRole;
import com.zosh.event.InvoiceEmailRequestedEvent;
import com.zosh.event.publisher.InvoiceEventPublisher;
import com.zosh.exception.BusinessValidationException;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.modal.Branch;
import com.zosh.modal.BranchSettings;
import com.zosh.modal.Customer;
import com.zosh.modal.Invoice;
import com.zosh.modal.InvoiceLineItem;
import com.zosh.modal.Order;
import com.zosh.modal.OrderItem;
import com.zosh.modal.Store;
import com.zosh.modal.User;
import com.zosh.payload.request.BranchSettingsRequest;
import com.zosh.payload.request.CompletePaymentRequest;
import com.zosh.payload.response.InvoiceResponse;
import com.zosh.repository.BranchSettingsRepository;
import com.zosh.repository.CustomerRepository;
import com.zosh.repository.InvoiceRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.service.InvoiceDocumentService;
import com.zosh.service.InvoiceService;
import com.zosh.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final BranchSettingsRepository branchSettingsRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final InvoiceDocumentService invoiceDocumentService;
    private final InvoiceEventPublisher invoiceEventPublisher;

    @Override
    @Transactional
    public InvoiceResponse completePaymentAndCreateInvoice(Long orderId, CompletePaymentRequest request) throws Exception {
        User actor = userService.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        authorizeOrderAccess(actor, order);

        if (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank()) {
            throw new BusinessValidationException("customerEmail", "Customer email is required to send invoice");
        }

        Customer customer = resolveCustomer(order, request);
        order.setCustomer(customer);
        if (request.getPaymentMethod() != null) {
            order.setPaymentType(request.getPaymentMethod());
        }
        orderRepository.save(order);

        Invoice existingInvoice = invoiceRepository.findByOrder_Id(orderId).orElse(null);
        if (existingInvoice != null) {
            return toResponse(existingInvoice);
        }

        Invoice invoice = buildInvoice(order, customer, request);
        invoice.setPdfContent(invoiceDocumentService.generatePdf(invoice));
        invoice.setPdfGeneratedAt(LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);

        if (Boolean.TRUE.equals(request.getSendEmail())) {
            invoiceEventPublisher.publishInvoiceEmailRequested(new InvoiceEmailRequestedEvent(invoice.getId()));
        }

        return toResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse resendInvoiceEmail(Long invoiceId) throws ResourceNotFoundException {
        User actor = userService.getCurrentUser();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + invoiceId));
        authorizeInvoiceAccess(actor, invoice);
        invoice.setDeliveryStatus(InvoiceDeliveryStatus.PENDING);
        invoice.setRetryCount(0);
        invoice.setLastError(null);
        invoiceRepository.save(invoice);
        invoiceEventPublisher.publishInvoiceEmailRequested(new InvoiceEmailRequestedEvent(invoice.getId()));
        return toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceStatus(Long invoiceId) throws ResourceNotFoundException {
        User actor = userService.getCurrentUser();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + invoiceId));
        authorizeInvoiceAccess(actor, invoice);
        return toResponse(invoice);
    }

    @Override
    @Transactional
    public byte[] getInvoicePdf(Long invoiceId) throws ResourceNotFoundException {
        User actor = userService.getCurrentUser();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + invoiceId));
        authorizeInvoiceAccess(actor, invoice);

        if (invoice.getPdfContent() == null || invoice.getPdfContent().length == 0) {
            invoice.setPdfContent(invoiceDocumentService.generatePdf(invoice));
            invoice.setPdfGeneratedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
        }
        return invoice.getPdfContent();
    }

    private Invoice buildInvoice(Order order, Customer customer, CompletePaymentRequest request) throws Exception {
        Branch branch = order.getBranch();
        Store store = branch.getStore();
        BranchSettings settings = branchSettingsRepository.findById(branch.getId()).orElse(null);
        BranchSettingsRequest.TaxSettings taxSettings = settings == null ? null : readJson(settings.getTaxJson(), BranchSettingsRequest.TaxSettings.class);

        List<OrderItem> sortedItems = order.getItems() == null ? List.of() : order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct() != null ? item.getProduct().getName() : ""))
                .toList();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();

        for (OrderItem orderItem : sortedItems) {
            BigDecimal lineSubtotal = toMoney(orderItem.getPrice());
            subtotal = subtotal.add(lineSubtotal);
            invoiceLineItems.add(InvoiceLineItem.builder()
                    .itemName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : "Item")
                    .sku(orderItem.getProduct() != null ? orderItem.getProduct().getSku() : null)
                    .quantity(orderItem.getQuantity() == null ? 0 : orderItem.getQuantity())
                    .unitPrice(calcUnitPrice(orderItem))
                    .taxPercent(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .taxAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .discountAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .lineTotal(lineSubtotal)
                    .build());
        }

        BigDecimal grandTotal = toMoney(order.getTotalAmount());
        BigDecimal taxTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (taxSettings != null && Boolean.TRUE.equals(taxSettings.getGstEnabled()) && taxSettings.getGstPercentage() != null) {
            BigDecimal desiredTax = grandTotal.subtract(subtotal);
            if (desiredTax.compareTo(BigDecimal.ZERO) <= 0) {
                desiredTax = subtotal.multiply(taxSettings.getGstPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                grandTotal = subtotal.add(desiredTax);
            }

            taxTotal = desiredTax.setScale(2, RoundingMode.HALF_UP);
            distributeTax(invoiceLineItems, subtotal, taxTotal, taxSettings.getGstPercentage());
        } else {
            grandTotal = subtotal;
        }

        Invoice invoice = Invoice.builder()
                .id(order.getId())
                .invoiceNumber(generateInvoiceNumber(order.getId()))
                .invoiceDateTime(LocalDateTime.now())
                .order(order)
                .storeName(store.getBrand())
                .storeAddress(resolveStoreAddress(store, branch))
                .storePhone(store.getContact() != null ? store.getContact().getPhone() : null)
                .storeEmail(store.getContact() != null ? store.getContact().getEmail() : null)
                .branchName(branch.getName())
                .branchAddress(branch.getAddress())
                .branchPhone(branch.getPhone())
                .branchEmail(branch.getEmail())
                .cashierName(order.getCashier() != null ? order.getCashier().getFullName() : null)
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().name() : (order.getPaymentType() != null ? order.getPaymentType().name() : null))
                .paymentReference(request.getPaymentReference())
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .taxTotal(taxTotal.setScale(2, RoundingMode.HALF_UP))
                .discountTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .grandTotal(grandTotal.setScale(2, RoundingMode.HALF_UP))
                .deliveryStatus(InvoiceDeliveryStatus.PENDING)
                .retryCount(0)
                .lineItems(invoiceLineItems)
                .build();

        for (InvoiceLineItem lineItem : invoiceLineItems) {
            lineItem.setInvoice(invoice);
        }

        return invoice;
    }

    private void distributeTax(List<InvoiceLineItem> lineItems, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal taxPercent) {
        if (lineItems.isEmpty() || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal allocated = BigDecimal.ZERO;
        for (int index = 0; index < lineItems.size(); index++) {
            InvoiceLineItem item = lineItems.get(index);
            BigDecimal taxAmount;
            if (index == lineItems.size() - 1) {
                taxAmount = taxTotal.subtract(allocated);
            } else {
                BigDecimal ratio = item.getLineTotal().divide(subtotal, 8, RoundingMode.HALF_UP);
                taxAmount = taxTotal.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                allocated = allocated.add(taxAmount);
            }
            item.setTaxPercent(taxPercent.setScale(2, RoundingMode.HALF_UP));
            item.setTaxAmount(taxAmount.setScale(2, RoundingMode.HALF_UP));
            item.setLineTotal(item.getLineTotal().add(taxAmount).setScale(2, RoundingMode.HALF_UP));
        }
    }

    private Customer resolveCustomer(Order order, CompletePaymentRequest request) throws ResourceNotFoundException {
        Customer customer = null;

        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + request.getCustomerId()));
        } else if (order.getCustomer() != null) {
            customer = order.getCustomer();
        } else if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            customer = customerRepository.findByEmailIgnoreCase(request.getCustomerEmail().trim());
        }

        if (customer == null) {
            customer = new Customer();
        }

        customer.setFullName(firstNonBlank(request.getCustomerName(), customer.getFullName(), "Walk-in Customer"));
        customer.setEmail(request.getCustomerEmail().trim().toLowerCase(Locale.ROOT));
        if (request.getCustomerPhone() != null) {
            String phone = request.getCustomerPhone().trim();
            customer.setPhone(phone.isBlank() ? null : phone);
        }

        return customerRepository.save(customer);
    }

    private void authorizeOrderAccess(User actor, Order order) {
        String role = actor.getRole().name();
        boolean allowed;

        if (role.equals("ROLE_STORE_ADMIN") || role.equals("ROLE_STORE_MANAGER")) {
            Store actorStore = actor.getStore() != null ? actor.getStore() : storeRepository.findByStoreAdminId(actor.getId());
            allowed = actorStore != null
                    && order.getBranch() != null
                    && order.getBranch().getStore() != null
                    && actorStore.getId().equals(order.getBranch().getStore().getId());
        } else if (role.equals("ROLE_BRANCH_MANAGER") || role.equals("ROLE_BRANCH_ADMIN") || role.equals("ROLE_BRANCH_CASHIER")) {
            allowed = actor.getBranch() != null
                    && order.getBranch() != null
                    && actor.getBranch().getId().equals(order.getBranch().getId());
        } else {
            allowed = false;
        }

        if (!allowed) {
            throw new AccessDeniedException("You are not allowed to complete payment for this order");
        }
    }

    private void authorizeInvoiceAccess(User actor, Invoice invoice) {
        if (invoice.getOrder() == null) {
            throw new AccessDeniedException("Invoice is not linked to an order");
        }
        authorizeOrderAccess(actor, invoice.getOrder());
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrder() != null ? invoice.getOrder().getId() : invoice.getId())
                .invoicePdfUrl("/api/invoices/" + invoice.getId() + "/pdf")
                .deliveryStatus(invoice.getDeliveryStatus() != null ? invoice.getDeliveryStatus().name() : null)
                .invoiceDateTime(invoice.getInvoiceDateTime())
                .emailSentAt(invoice.getEmailSentAt())
                .retryCount(invoice.getRetryCount())
                .lastError(invoice.getLastError())
                .customerEmail(invoice.getCustomerEmail())
                .paymentMethod(invoice.getPaymentMethod())
                .paymentReference(invoice.getPaymentReference())
                .message(messageForStatus(invoice.getDeliveryStatus()))
                .build();
    }

    private String messageForStatus(InvoiceDeliveryStatus status) {
        if (status == null) {
            return "Invoice created";
        }
        return switch (status) {
            case PENDING -> "Invoice created and email queued";
            case SENT -> "Invoice email sent";
            case FAILED -> "Invoice email failed";
        };
    }

    private String generateInvoiceNumber(Long id) {
        return String.format("INV-%d-%06d", Year.now().getValue(), id);
    }

    private BigDecimal toMoney(Double value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcUnitPrice(OrderItem item) {
        if (item.getQuantity() != null && item.getQuantity() > 0 && item.getPrice() != null) {
            return BigDecimal.valueOf(item.getPrice() / item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveStoreAddress(Store store, Branch branch) {
        if (store.getContact() != null && store.getContact().getAddress() != null && !store.getContact().getAddress().isBlank()) {
            return store.getContact().getAddress();
        }
        return branch.getAddress();
    }

    private <T> T readJson(String value, Class<T> type) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize branch settings", ex);
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "Walk-in Customer";
    }
}
