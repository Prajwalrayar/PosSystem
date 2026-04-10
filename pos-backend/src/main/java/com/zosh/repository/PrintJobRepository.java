package com.zosh.repository;

import com.zosh.modal.PrintJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrintJobRepository extends JpaRepository<PrintJob, UUID> {
    void deleteByCreatedById(Long createdById);
}
