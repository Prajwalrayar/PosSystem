package com.zosh.repository;

import com.zosh.modal.ReturnSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReturnSessionRepository extends JpaRepository<ReturnSession, UUID> {
}
