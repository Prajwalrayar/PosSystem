package com.zosh.repository;

import com.zosh.modal.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExportJobRepository extends JpaRepository<ExportJob, UUID> {
    void deleteByCreatedById(Long createdById);
}
