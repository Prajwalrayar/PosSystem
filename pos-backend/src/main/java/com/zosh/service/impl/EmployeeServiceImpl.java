package com.zosh.service.impl;

import com.zosh.domain.UserRole;
import com.zosh.exception.BusinessValidationException;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.exception.UserException;
import com.zosh.mapper.UserMapper;
import com.zosh.modal.Branch;
import com.zosh.modal.Store;
import com.zosh.modal.User;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.ResetEmployeePasswordRequest;
import com.zosh.payload.response.EmployeeActivityLogResponse;
import com.zosh.payload.response.EmployeePerformanceResponse;
import com.zosh.payload.response.EmployeePasswordResetResponse;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.ExportJobRepository;
import com.zosh.repository.LoyaltyTransactionRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PrintJobRepository;
import com.zosh.repository.RefundRepository;
import com.zosh.repository.ShiftReportRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.EmployeeService;
import com.zosh.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    private final UserRepository userRepository;


    private final StoreRepository storeRepository;

    private final BranchRepository branchRepository;
    private final ExportJobRepository exportJobRepository;
    private final PrintJobRepository printJobRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final ShiftReportRepository shiftReportRepository;

    @Override
    @Transactional
    public UserDTO createStoreEmployee(UserDTO dto, Long storeId) throws Exception {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

        Branch branch = null;

        if (isBranchScopedRole(dto.getRole())) {
            if (dto.getBranchId() == null) {
                throw new IllegalArgumentException("Branch ID is required for branch roles.");
            }

            branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found with ID: " + dto.getBranchId()));
        } else if (dto.getBranchId() != null) {
            throw new IllegalArgumentException("Branch ID can only be used for branch roles.");
        }

        User employee = UserMapper.toEntity(dto);
        employee.setStore(store);
        employee.setBranch(branch);
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        System.out.println("employee: " + employee);

        User isExist=userRepository.findByEmail(dto.getEmail());

        System.out.println("isExist: " + isExist);
        if(isExist!=null){
            employee.setId(isExist.getId());
        }

        User savedEmployee = userRepository.save(employee);

        System.out.println("savedEmployee: " + savedEmployee);

        // Assign manager to the branch if applicable
        if (dto.getRole() == UserRole.ROLE_BRANCH_MANAGER && branch != null) {
            branch.setManager(savedEmployee);
            branchRepository.save(branch); // make sure manager is saved
        }

        return UserMapper.toDTO(savedEmployee);
    }

    @Override
    public User createBranchEmployee(User employee, Long branchId) throws Exception {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));

        if (!(employee.getRole().equals(UserRole.ROLE_BRANCH_CASHIER) || employee.getRole().equals(UserRole.ROLE_BRANCH_MANAGER))) {
            throw new UserException("Invalid role for branch employee. Must be ROLE_BRANCH_ADMIN or ROLE_BRANCH_MANAGER");
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.setBranch(branch);

        User isExist=userRepository.findByEmail(employee.getEmail());
        if(isExist!=null){
            employee.setId(isExist.getId());
        }

        return userRepository.save(employee);
    }

    @Override
    public User updateEmployee(Long employeeId, User employeeDetails) throws Exception {
        User existingEmployee = findEmployeeById(employeeId);

        if (employeeDetails.getFullName() != null) {
            existingEmployee.setFullName(employeeDetails.getFullName());
        }
        if (employeeDetails.getEmail() != null) {
            existingEmployee.setEmail(employeeDetails.getEmail());
        }
        if (employeeDetails.getPhone() != null) {
            existingEmployee.setPhone(employeeDetails.getPhone());
        }
        if (employeeDetails.getRole() != null) {
            // Add logic to restrict role changes based on current user's role if necessary
            existingEmployee.setRole(employeeDetails.getRole());
        }
        // Password should be updated via a separate method for security reasons

        return userRepository.save(existingEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long employeeId) throws Exception {
        User employee = findEmployeeById(employeeId);

        exportJobRepository.deleteByCreatedById(employeeId);
        printJobRepository.deleteByCreatedById(employeeId);
        loyaltyTransactionRepository.deleteByCreatedById(employeeId);
        orderRepository.clearCashierById(employeeId);
        refundRepository.clearCashierById(employeeId);
        shiftReportRepository.clearCashierById(employeeId);

        if (employee.getBranch() != null && employee.getBranch().getManager() != null
                && employee.getBranch().getManager().getId().equals(employee.getId())) {
            Branch branch = employee.getBranch();
            branch.setManager(null);
            branchRepository.save(branch);
        }

        if (employee.getStore() != null && employee.getStore().getStoreAdmin() != null
                && employee.getStore().getStoreAdmin().getId().equals(employee.getId())) {
            Store store = employee.getStore();
            store.setStoreAdmin(null);
            storeRepository.save(store);
        }

        userRepository.delete(employee);
    }

    @Override
    public User findEmployeeById(Long employeeId) throws Exception {
        Optional<User> opt = userRepository.findById(employeeId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
    }

    @Override
    public List<User> findStoreEmployees(Long storeId, UserRole role) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
        return userRepository.findByStoreAndRoleIn(store, List.of(
                UserRole.ROLE_STORE_ADMIN,
                UserRole.ROLE_BRANCH_MANAGER,
                UserRole.ROLE_STORE_MANAGER
        ));
    }

    @Override
    public List<User> findBranchEmployees(Long branchId, UserRole role) throws Exception {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));
        List<User> employees = userRepository.findByBranchId(branch.getId()).stream()
                .filter(user -> role == null || user.getRole() == role)
                .collect(Collectors.toList());

        return employees;
    }

    @Override
    public EmployeePasswordResetResponse resetEmployeePassword(Long employeeId, ResetEmployeePasswordRequest request) throws Exception {
        User actor = userService.getCurrentUser();
        if (!(actor.getRole() == UserRole.ROLE_BRANCH_ADMIN || actor.getRole() == UserRole.ROLE_BRANCH_MANAGER)) {
            throw new AccessDeniedException("You are not allowed to reset employee passwords");
        }
        User employee = findEmployeeById(employeeId);
        if (employee.getBranch() == null || actor.getBranch() == null
                || !employee.getBranch().getId().equals(actor.getBranch().getId())) {
            throw new AccessDeniedException("You can reset passwords only for employees in your branch");
        }
        if (!(employee.getRole() == UserRole.ROLE_BRANCH_CASHIER || employee.getRole() == UserRole.ROLE_BRANCH_MANAGER)) {
            throw new BusinessValidationException("employeeId", "Only branch employees can be reset through this endpoint");
        }
        if (!PASSWORD_PATTERN.matcher(request.getTemporaryPassword()).matches()) {
            throw new BusinessValidationException(
                    "temporaryPassword",
                    "Temporary password must be at least 8 characters and include upper, lower, digit, and special character"
            );
        }

        employee.setPassword(passwordEncoder.encode(request.getTemporaryPassword()));
        employee.setForcePasswordChange(Boolean.TRUE.equals(request.getForceChangeOnNextLogin()));
        employee.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(employee);

        return new EmployeePasswordResetResponse(employee.getId(), employee.getForcePasswordChange());
    }

    @Override
    public EmployeePerformanceResponse getEmployeePerformance(Long employeeId, LocalDate from, LocalDate to) throws Exception {
        User actor = userService.getCurrentUser();
        if (!(actor.getRole() == UserRole.ROLE_BRANCH_ADMIN || actor.getRole() == UserRole.ROLE_BRANCH_MANAGER)) {
            throw new AccessDeniedException("You are not allowed to view employee performance");
        }

        User employee = findEmployeeById(employeeId);
        if (actor.getBranch() == null || employee.getBranch() == null
                || !actor.getBranch().getId().equals(employee.getBranch().getId())) {
            throw new AccessDeniedException("You can view performance only for employees in your branch");
        }

        LocalDate startDate = from == null ? LocalDate.now().minusDays(30) : from;
        LocalDate endDate = to == null ? LocalDate.now() : to;
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        var orders = orderRepository.findByCashierAndCreatedAtBetween(employee, start, end);
        var refunds = refundRepository.findByCashierAndCreatedAtBetween(employee, start, end);
        var shifts = shiftReportRepository.findByCashier(employee).stream()
                .filter(shift -> shift.getShiftStart() != null
                        && !shift.getShiftStart().isBefore(start)
                        && shift.getShiftStart().isBefore(end))
                .toList();

        long ordersProcessed = orders.size();
        double totalSales = orders.stream().mapToDouble(order -> order.getTotalAmount() == null ? 0D : order.getTotalAmount()).sum();
        double averageOrderValue = ordersProcessed == 0 ? 0D : totalSales / ordersProcessed;

        List<EmployeePerformanceResponse.EmployeePerformanceDailySales> dailySales = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate(),
                        Collectors.summingDouble(order -> order.getTotalAmount() == null ? 0D : order.getTotalAmount())))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> new EmployeePerformanceResponse.EmployeePerformanceDailySales(
                        entry.getKey().toString(),
                        entry.getValue()
                ))
                .toList();

        List<EmployeeActivityLogResponse> activityLog = java.util.stream.Stream.concat(
                        shifts.stream().map(shift -> new EmployeeActivityLogResponse(
                                shift.getShiftStart(),
                                "Started shift at branch " + shift.getBranch().getName()
                        )),
                        refunds.stream().map(refund -> new EmployeeActivityLogResponse(
                                refund.getCreatedAt(),
                                "Processed refund for order " + refund.getOrder().getId()
                        ))
                )
                .sorted(Comparator.comparing(EmployeeActivityLogResponse::getAt).reversed())
                .limit(20)
                .toList();

        return new EmployeePerformanceResponse(
                ordersProcessed,
                totalSales,
                averageOrderValue,
                dailySales,
                activityLog
        );
    }

    private boolean isBranchScopedRole(UserRole role) {
        return role == UserRole.ROLE_BRANCH_ADMIN
                || role == UserRole.ROLE_BRANCH_MANAGER
                || role == UserRole.ROLE_BRANCH_CASHIER;
    }
}
