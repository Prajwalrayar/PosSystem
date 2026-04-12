package com.zosh.repository;

import com.zosh.modal.Invoice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrder_Id(Long orderId);
    @EntityGraph(attributePaths = {"lineItems", "order"})
    Optional<Invoice> findDetailedById(Long id);
    boolean existsByOrder_Id(Long orderId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
