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
import java.util.Set;

public interface UserRepository extends JpaRepository<Users, Long> {

    Users findByEmail(String email);
    List<Users> findByStore(Store store);
    List<Users> findByBranchId(Long  branchId);

    Set<Users> findByRole(UserRole role);
    List<Users>findByStoreId(Long storeId);
    List<Users> findByStoreAndRoleIn(com.capstone.model.Store store, List<UserRole> roles);
    List<Users> findByBranchAndRoleIn(com.capstone.model.Branch branch, List<UserRole> roles);

    //	analysis
    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.id IN (
            SELECT s.storeAdmin.id FROM Store s WHERE s.storeAdmin.id = :storeAdminId
        )
        AND u.role IN (:roles)
    """)
    int countByStoreAdminIdAndRoles(@Param("storeAdminId") Long storeAdminId,
                                    @Param("roles") List<UserRole> roles);

    @Query("""
    SELECT new com.zosh.payload.dto.UserDTO(
		u.id,
		u.email,
		u.fullName, u.role, u.branch.name, u.lastLogin
    )
    FROM User u
    WHERE u.lastLogin < :cutoffDate
    AND u.branch.store.storeAdmin.id = :storeAdminId
    AND u.role = com.zosh.domain.UserRole.ROLE_BRANCH_CASHIER
""")
    List<UserDto> findInactiveCashiers(@Param("storeAdminId") Long storeAdminId,
                                       @Param("cutoffDate") LocalDateTime cutoffDate);

}
