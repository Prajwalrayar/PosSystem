package com.zosh;

import com.zosh.modal.Invoice;
import com.zosh.modal.InvoiceLineItem;
import com.zosh.service.InvoiceDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceDocumentServiceTests {

    @Test
    void buildHtmlUsesEmailFriendlyTableLayout() {
        InvoiceDocumentService service = new InvoiceDocumentService();
        ReflectionTestUtils.setField(service, "brandName", "SmartPos");

        Invoice invoice = invoice();

        String html = service.buildHtml(invoice);
        String plainText = service.buildPlainText(invoice);

        assertThat(html).contains("<table class=\"items\">");
        assertThat(html).contains("<table class=\"summary\">");
        assertThat(html).contains("Bill To");
        assertThat(html).contains("Store / Branch");
        assertThat(html).contains("Payment");
        assertThat(html).contains("Cashier");
        assertThat(html).doesNotContain("display:flex");
        assertThat(html).doesNotContain("justify-content:space-between");

        assertThat(plainText).contains("Invoice No: INV-2026-000026");
        assertThat(plainText).contains("Bill To");
        assertThat(plainText).contains("Items");
        assertThat(plainText).contains("Payment");
    }

    private Invoice invoice() {
        InvoiceLineItem item = InvoiceLineItem.builder()
                .itemName("demo")
                .sku("2345")
                .quantity(1)
                .unitPrice(new BigDecimal("1221.96"))
                .taxPercent(new BigDecimal("56.00"))
                .taxAmount(new BigDecimal("684.30"))
                .discountAmount(BigDecimal.ZERO)
                .lineTotal(new BigDecimal("1906.26"))
                .build();

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-2026-000026")
                .invoiceDateTime(LocalDateTime.of(2026, 4, 12, 15, 14))
                .storeName("store")
                .storeAddress("branch1")
                .storePhone("12345678")
                .storeEmail("branch1@gmail.com")
                .storeGstin("GST-STORE")
                .branchName("branch1")
                .branchAddress("branch1")
                .branchPhone("12345678")
                .branchEmail("branch1@gmail.com")
                .branchGstin("GST-BRANCH")
                .customerName("sush")
                .customerEmail("sushantmediawork@gmail.com")
                .customerPhone("6366660233")
                .cashierName("s1b1c2")
                .paymentMethod("CASH")
                .paymentReference("-")
                .subtotal(new BigDecimal("1221.96"))
                .taxTotal(new BigDecimal("684.30"))
                .discountTotal(BigDecimal.ZERO)
                .grandTotal(new BigDecimal("1906.26"))
                .lineItems(List.of(item))
                .build();

        item.setInvoice(invoice);
        return invoice;
    }
}
