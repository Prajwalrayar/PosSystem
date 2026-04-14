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

        applyPaymentBreakdown(order, request);
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

        BigDecimal subtotal = order.getSubtotal() != null
                ? toMoney(order.getSubtotal())
                : order.getTotalAmount() != null
                ? toMoney(order.getTotalAmount())
                : sumOrderItemTotals(sortedItems);
        List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();

        for (OrderItem orderItem : sortedItems) {
            BigDecimal lineSubtotal = toMoney(orderItem.getPrice());
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

        BigDecimal taxTotal = resolveTaxTotal(order, request, subtotal, taxSettings);
        BigDecimal discountTotal = resolveDiscountTotal(order, request, subtotal, taxTotal);
        BigDecimal grandTotal = resolveGrandTotal(order, request, subtotal, taxTotal, discountTotal);

        distributeTax(invoiceLineItems, subtotal, taxTotal, resolveTaxRate(order, request, taxSettings));
        distributeDiscount(invoiceLineItems, subtotal, discountTotal);

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
                .discountTotal(discountTotal.setScale(2, RoundingMode.HALF_UP))
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

    private void distributeDiscount(List<InvoiceLineItem> lineItems, BigDecimal subtotal, BigDecimal discountTotal) {
        if (lineItems.isEmpty() || subtotal.compareTo(BigDecimal.ZERO) <= 0 || discountTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal allocated = BigDecimal.ZERO;
        for (int index = 0; index < lineItems.size(); index++) {
            InvoiceLineItem item = lineItems.get(index);
            BigDecimal discountAmount;
            if (index == lineItems.size() - 1) {
                discountAmount = discountTotal.subtract(allocated);
            } else {
                BigDecimal ratio = item.getLineTotal().divide(subtotal, 8, RoundingMode.HALF_UP);
                discountAmount = discountTotal.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                allocated = allocated.add(discountAmount);
            }

            discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP);
            item.setDiscountAmount(discountAmount);
            item.setLineTotal(item.getLineTotal().subtract(discountAmount).setScale(2, RoundingMode.HALF_UP));
        }
    }

    private Customer resolveCustomer(Order order, CompletePaymentRequest request) throws ResourceNotFoundException {
        if (order.getBranch() == null || order.getBranch().getStore() == null) {
            throw new ResourceNotFoundException("Store not found for order " + order.getId());
        }

        Store store = order.getBranch().getStore();
        Customer customer = null;

        if (request.getCustomerId() != null) {
            customer = customerRepository.findByStore_IdAndId(store.getId(), request.getCustomerId());
            if (customer == null) {
                throw new ResourceNotFoundException("Customer not found with id " + request.getCustomerId());
            }
        } else if (order.getCustomer() != null) {
            if (order.getCustomer().getStore() != null
                    && order.getCustomer().getStore().getId() != null
                    && !order.getCustomer().getStore().getId().equals(store.getId())) {
                throw new AccessDeniedException("Customer does not belong to this store");
            }
            customer = order.getCustomer();
        } else if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            customer = customerRepository.findByStore_IdAndEmailIgnoreCase(store.getId(), request.getCustomerEmail().trim());
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
        customer.setStore(store);

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
                .subtotal(invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : null)
                .taxTotal(invoice.getTaxTotal() != null ? invoice.getTaxTotal().doubleValue() : null)
                .discountTotal(invoice.getDiscountTotal() != null ? invoice.getDiscountTotal().doubleValue() : null)
                .grandTotal(invoice.getGrandTotal() != null ? invoice.getGrandTotal().doubleValue() : null)
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

    private void applyPaymentBreakdown(Order order, CompletePaymentRequest request) {
        BigDecimal subtotal = resolveSubtotal(order, request);
        BigDecimal taxTotal = resolveTaxTotal(order, request, subtotal, null);
        BigDecimal discountTotal = resolveDiscountTotal(order, request, subtotal, taxTotal);
        BigDecimal grandTotal = resolveGrandTotal(order, request, subtotal, taxTotal, discountTotal);

        order.setSubtotal(subtotal.doubleValue());
        order.setTaxRate(resolveTaxRate(order, request, null).doubleValue());
        order.setTaxAmount(taxTotal.doubleValue());
        order.setDiscountType(firstNonBlankOrNull(request.getDiscountType(), order.getDiscountType()));
        order.setDiscountValue(resolveDiscountValue(order, request, subtotal).doubleValue());
        order.setDiscountAmount(discountTotal.doubleValue());
        order.setTotalAmount(grandTotal.doubleValue());
    }

    private BigDecimal resolveSubtotal(Order order, CompletePaymentRequest request) {
        if (request.getSubtotal() != null) {
            return toMoney(request.getSubtotal());
        }
        if (order.getSubtotal() != null) {
            return toMoney(order.getSubtotal());
        }
        return toMoney(order.getTotalAmount());
    }

    private BigDecimal resolveTaxTotal(Order order, CompletePaymentRequest request, BigDecimal subtotal, BranchSettingsRequest.TaxSettings taxSettings) {
        if (request.getTaxAmount() != null) {
            return toMoney(request.getTaxAmount());
        }
        if (order.getTaxAmount() != null) {
            return toMoney(order.getTaxAmount());
        }
        BigDecimal taxRate = resolveTaxRate(order, request, taxSettings);
        if (taxRate.compareTo(BigDecimal.ZERO) > 0 && subtotal.compareTo(BigDecimal.ZERO) > 0) {
            return subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveDiscountTotal(Order order, CompletePaymentRequest request, BigDecimal subtotal, BigDecimal taxTotal) {
        if (request.getDiscountAmount() != null) {
            return toMoney(request.getDiscountAmount());
        }
        if (order.getDiscountAmount() != null) {
            return toMoney(order.getDiscountAmount());
        }

        BigDecimal discountValue = resolveDiscountValue(order, request, subtotal);
        String discountType = firstNonBlankOrNull(request.getDiscountType(), order.getDiscountType());
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0 || discountType == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if ("percentage".equalsIgnoreCase(discountType) || "percent".equalsIgnoreCase(discountType)) {
            return subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return discountValue.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveGrandTotal(Order order, CompletePaymentRequest request, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal discountTotal) {
        if (request.getTotalAmount() != null) {
            return toMoney(request.getTotalAmount());
        }
        if (order.getTotalAmount() != null) {
            return toMoney(order.getTotalAmount());
        }
        BigDecimal total = subtotal.add(taxTotal).subtract(discountTotal);
        return total.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveTaxRate(Order order, CompletePaymentRequest request, BranchSettingsRequest.TaxSettings taxSettings) {
        if (request.getTaxRate() != null) {
            return toMoney(request.getTaxRate());
        }
        if (order.getTaxRate() != null) {
            return toMoney(order.getTaxRate());
        }
        if (taxSettings != null && Boolean.TRUE.equals(taxSettings.getGstEnabled()) && taxSettings.getGstPercentage() != null) {
            return taxSettings.getGstPercentage().setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveDiscountValue(Order order, CompletePaymentRequest request, BigDecimal subtotal) {
        if (request.getDiscountValue() != null) {
            return toMoney(request.getDiscountValue());
        }
        if (order.getDiscountValue() != null) {
            return toMoney(order.getDiscountValue());
        }
        BigDecimal discountAmount = request.getDiscountAmount() != null ? toMoney(request.getDiscountAmount()) : toMoney(order.getDiscountAmount());
        String discountType = firstNonBlank(request.getDiscountType(), order.getDiscountType(), null);
        if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (discountType != null && ("percentage".equalsIgnoreCase(discountType) || "percent".equalsIgnoreCase(discountType)) && subtotal.compareTo(BigDecimal.ZERO) > 0) {
            return discountAmount.multiply(BigDecimal.valueOf(100)).divide(subtotal, 2, RoundingMode.HALF_UP);
        }
        return discountAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveStoreAddress(Store store, Branch branch) {
        if (store.getContact() != null && store.getContact().getAddress() != null && !store.getContact().getAddress().isBlank()) {
            return store.getContact().getAddress();
        }
        return branch.getAddress();
    }

    private BigDecimal sumOrderItemTotals(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(toMoney(item.getPrice()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
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

    private String firstNonBlankOrNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
