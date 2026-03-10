package com.capstone.repository;

import com.capstone.domain.UserRole;
import com.capstone.model.Branch;
import com.capstone.model.Store;
import com.capstone.model.Users;
import com.capstone.payload.dto.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<Users, Long> {

    Users findByEmail(String email);
    List<Users> findByStore(Store store);
    List<Users> findByBranchId(Long  branchId);

    @Query("""
        SELECT COUNT(u)
        FROM Users u
        WHERE u.id IN (
            SELECT s.storeAdmin.id FROM Store s WHERE s.storeAdmin.id = :storeAdminId
        )
        AND u.role IN (:roles)
    """)
    int countByStoreAdminIdAndRoles(@Param("storeAdminId") Long storeAdminId,
                                    @Param("roles") List<UserRole> roles);

    @Query("""
    SELECT u.fullName
    FROM Users u
    WHERE u.lastLogin < :cutoffDate
    AND u.id IN(
        select u2.storeAdmin.id from Store u2 WHERE u2.storeAdmin.id = :storeAdminId
        )
    AND u.role = com.capstone.domain.UserRole.ROLE_BRANCH_CASHIER
""")
    List<UserDto> findInactiveCashiers(@Param("storeAdminId") Long storeAdminId,
                                       @Param("cutoffDate") LocalDateTime cutoffDate);
}
