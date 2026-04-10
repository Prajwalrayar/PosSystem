package com.zosh.repository;

import com.zosh.modal.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    void deleteByCreatedById(Long createdById);
}
