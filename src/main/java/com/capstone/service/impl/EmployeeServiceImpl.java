package com.capstone.service.impl;

import com.capstone.domain.UserRole;
import com.capstone.mapper.UserMapper;
import com.capstone.model.Branch;
import com.capstone.model.Store;
import com.capstone.model.Users;
import com.capstone.payload.dto.UserDto;
import com.capstone.repository.BranchRepository;
import com.capstone.repository.StoreRepository;
import com.capstone.repository.UserRepository;
import com.capstone.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto createStoreEmployee(UserDto employee, Long storeId) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(
                ()-> new Exception("Store does not Exist...")
        );

        Branch branch = null;

        if(employee.getRole()==UserRole.ROLE_BRANCH_MANAGER){
            if(employee.getBranchId()==null){
                throw new Exception("Branch Id is required to create employees");
            }
            branch = branchRepository.findById(employee.getBranchId()).orElseThrow(
                    ()-> new Exception("Branch not Found!")
            );
        }
        Users user = UserMapper.toEntity(employee);
        user.setStore(store);
        user.setBranch(branch);
        user.setPassword(passwordEncoder.encode(employee.getPassword()));
        Users savedEmployee = userRepository.save(user);
        if(employee.getRole()==UserRole.ROLE_BRANCH_MANAGER && branch != null){
            branch.setManager(savedEmployee);
            branchRepository.save(branch);
        }
        return UserMapper.toDTO(savedEmployee);
    }

    @Override
    public UserDto createBranchEmployee(UserDto employee, Long branchId) throws Exception {
        Branch branch = branchRepository.findById(branchId).orElseThrow(
                ()-> new Exception("Branch not Found!")
        );
        if(employee.getRole()==UserRole.ROLE_BRANCH_MANAGER ||
        employee.getRole()==UserRole.ROLE_CASHIER){
            Users user = UserMapper.toEntity(employee);
            user.setBranch(branch);
            user.setPassword(passwordEncoder.encode(employee.getPassword()));
            return UserMapper.toDTO(userRepository.save(user));
        }
        throw new Exception("Branch role is not supported");
    }

    @Override
    public Users updateEmployee(Long employeeId, UserDto employeeDetails) throws Exception {
        Users existingEmployee = userRepository.findById(employeeId).orElseThrow(
                ()-> new Exception("Employee not Found")
        );
        Branch branch = branchRepository.findById(employeeDetails.getBranchId()).orElseThrow(
                ()-> new Exception("Branch not Found")
        );

        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setFullName(employeeDetails.getFullName());
        existingEmployee.setPassword(employeeDetails.getPassword());
        existingEmployee.setRole(employeeDetails.getRole());
        existingEmployee.setBranch(branch);
        return userRepository.save(existingEmployee);
    }

    @Override
    public void deleteEmployee(Long employeeId) throws Exception {
        Users emp =  userRepository.findById(employeeId).orElseThrow(
                ()->new Exception("Employee not found")
        );
        userRepository.delete(emp);
    }

    @Override
    public List<UserDto> findStoreEmployees(Long storeId, UserRole role) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(
                ()-> new Exception("Store not found")
        );
        return userRepository.findByStore(store).stream().filter(
                users -> role == null || users.getRole()==role
        ).map(UserMapper::toDTO).collect(Collectors.toList());

    }

    @Override
    public List<UserDto> findBranchEmployees(Long branchId, UserRole role) throws Exception {
        Branch branch = branchRepository.findById(branchId).orElseThrow(
                ()-> new Exception("Branch not found")
        );

        return userRepository.findByBranchId(branchId).stream().filter(
                users -> role == null || users.getRole()==role
        ).map(UserMapper::toDTO).collect(Collectors.toList());
    }

}
