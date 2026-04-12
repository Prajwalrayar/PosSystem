package com.zosh.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zosh.domain.InvoiceDeliveryStatus;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDateTime invoiceDateTime;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "order_id", unique = true)
    @JsonIgnore
    private Order order;

    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String storeEmail;
    private String storeGstin;

    private String branchName;
    private String branchAddress;
    private String branchPhone;
    private String branchEmail;
    private String branchGstin;

    private String cashierName;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String paymentMethod;
    private String paymentReference;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private InvoiceDeliveryStatus deliveryStatus = InvoiceDeliveryStatus.PENDING;

    private LocalDateTime emailSentAt;

    @Builder.Default
    @Column(nullable = false)
    private Integer retryCount = 0;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] pdfContent;

    private LocalDateTime pdfGeneratedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    private String lastError;

    @PrePersist
    public void onCreate() {
        if (invoiceDateTime == null) {
            invoiceDateTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onUpdate() {
        if (invoiceDateTime == null) {
            invoiceDateTime = LocalDateTime.now();
        }
    }
}
