package com.capstone.repository;

import com.capstone.domain.UserRole;
import com.capstone.model.Branch;
import com.capstone.model.Store;
import com.capstone.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, Long> {

    Users findByEmail(String email);
    List<Users> findByStore(Store store);
    List<Users> findByBranchId(Long  branchId);
}
