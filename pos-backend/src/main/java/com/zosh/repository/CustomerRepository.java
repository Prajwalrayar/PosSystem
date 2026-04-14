package com.zosh.repository;

import com.zosh.modal.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email);

    Customer findByEmailIgnoreCase(String email);
    Customer findByStore_IdAndId(Long storeId, Long id);
    Customer findByStore_IdAndEmailIgnoreCase(Long storeId, String email);
    List<Customer> findByStore_Id(Long storeId);

    @Query("""
        SELECT c
        FROM Customer c
        WHERE c.store.id = :storeId
          AND (
            LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    List<Customer> searchByStoreIdAndKeyword(@Param("storeId") Long storeId,
                                             @Param("keyword") String keyword);

//    analysis
@Query("""
        SELECT COUNT(c)
        FROM Customer c
        WHERE c.store.storeAdmin.id = :storeAdminId
    """)
int countByStoreAdminId(@Param("storeAdminId") Long storeAdminId);
}
