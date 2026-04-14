package com.zosh;

import com.zosh.domain.UserRole;
import com.zosh.exception.BusinessValidationException;
import com.zosh.modal.Branch;
import com.zosh.modal.Customer;
import com.zosh.modal.LoyaltyTransaction;
import com.zosh.modal.LoyaltyTransactionType;
import com.zosh.modal.User;
import com.zosh.payload.request.ChangePasswordRequest;
import com.zosh.payload.request.LoyaltyTransactionRequest;
import com.zosh.payload.request.ResetEmployeePasswordRequest;
import com.zosh.payload.request.UpdateProfileRequest;
import com.zosh.payload.response.EmployeePasswordResetResponse;
import com.zosh.payload.response.LoyaltyTransactionResponse;
import com.zosh.payload.response.ProfileResponse;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.CustomerRepository;
import com.zosh.repository.ExportJobRepository;
import com.zosh.repository.LoyaltyTransactionRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PasswordResetTokenRepository;
import com.zosh.repository.PrintJobRepository;
import com.zosh.repository.RefundRepository;
import com.zosh.repository.ShiftReportRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.UserService;
import com.zosh.service.impl.CustomerServiceImpl;
import com.zosh.service.impl.EmployeeServiceImpl;
import com.zosh.service.impl.UserServiceImpl;
import com.zosh.configrations.JwtProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class P0ServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserServiceImpl userService;

    @Mock
    private BranchRepository branchRepository;
    @Mock
    private com.zosh.repository.StoreRepository storeRepository;
    @Mock
    private ExportJobRepository exportJobRepository;
    @Mock
    private PrintJobRepository printJobRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private ShiftReportRepository shiftReportRepository;

    @Mock
    private UserService delegatedUserService;

    private EmployeeServiceImpl employeeService;

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                passwordEncoder,
                jwtProvider,
                passwordResetTokenRepository
        );
        employeeService = new EmployeeServiceImpl(
                userRepository,
                storeRepository,
                branchRepository,
                exportJobRepository,
                printJobRepository,
                loyaltyTransactionRepository,
                passwordEncoder,
                delegatedUserService,
                orderRepository,
                refundRepository,
                shiftReportRepository
        );
        customerService = new CustomerServiceImpl(
                customerRepository,
                loyaltyTransactionRepository,
                storeRepository,
                delegatedUserService
        );
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void changePasswordUpdatesEncodedPasswordAndFlags() {
        User currentUser = user(1L, "admin@pos.com", UserRole.ROLE_ADMIN);
        currentUser.setPassword(passwordEncoder.encode("OldPass@123"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser.getEmail(), null)
        );
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(currentUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass@123");
        request.setNewPassword("NewPass@123");
        request.setConfirmPassword("NewPass@123");

        userService.changePassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(passwordEncoder.matches("NewPass@123", saved.getPassword())).isTrue();
        assertThat(saved.getForcePasswordChange()).isFalse();
        assertThat(saved.getPasswordChangedAt()).isNotNull();
    }

    @Test
    void updateCurrentUserProfileReturnsUpdatedProfile() {
        User currentUser = user(2L, "manager@pos.com", UserRole.ROLE_STORE_ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser.getEmail(), null)
        );
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(currentUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Name");
        request.setPhone("+919999999999");

        ProfileResponse response = userService.updateCurrentUserProfile(request);

        assertThat(response.getFullName()).isEqualTo("New Name");
        assertThat(response.getMobile()).isEqualTo("+919999999999");
    }

    @Test
    void resetEmployeePasswordRejectsDifferentBranch() throws Exception {
        Branch actorBranch = branch(11L, "A");
        Branch employeeBranch = branch(12L, "B");
        User actor = user(3L, "branch@pos.com", UserRole.ROLE_BRANCH_ADMIN);
        actor.setBranch(actorBranch);
        User employee = user(4L, "cashier@pos.com", UserRole.ROLE_BRANCH_CASHIER);
        employee.setBranch(employeeBranch);

        when(delegatedUserService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        ResetEmployeePasswordRequest request = new ResetEmployeePasswordRequest();
        request.setTemporaryPassword("TempPass@123");
        request.setForceChangeOnNextLogin(true);

        assertThatThrownBy(() -> employeeService.resetEmployeePassword(employee.getId(), request))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void resetEmployeePasswordSucceedsForSameBranch() throws Exception {
        Branch branch = branch(11L, "Main");
        User actor = user(3L, "branch@pos.com", UserRole.ROLE_BRANCH_ADMIN);
        actor.setBranch(branch);
        User employee = user(4L, "cashier@pos.com", UserRole.ROLE_BRANCH_CASHIER);
        employee.setBranch(branch);
        employee.setPassword(passwordEncoder.encode("OldPass@123"));

        when(delegatedUserService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResetEmployeePasswordRequest request = new ResetEmployeePasswordRequest();
        request.setTemporaryPassword("TempPass@123");
        request.setForceChangeOnNextLogin(true);

        EmployeePasswordResetResponse response = employeeService.resetEmployeePassword(employee.getId(), request);

        assertThat(response.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(response.getForceChangeOnNextLogin()).isTrue();
    }

    @Test
    void createLoyaltyTransactionRejectsNegativeResultingBalance() {
        User actor = user(5L, "cashier@pos.com", UserRole.ROLE_BRANCH_CASHIER);
        com.zosh.modal.Store store = new com.zosh.modal.Store();
        store.setId(77L);
        actor.setStore(store);
        Customer customer = new Customer();
        customer.setId(10L);
        customer.setLoyaltyPoints(20);
        customer.setStore(store);

        when(delegatedUserService.getCurrentUser()).thenReturn(actor);
        when(customerRepository.findByStore_IdAndId(store.getId(), customer.getId())).thenReturn(customer);

        LoyaltyTransactionRequest request = new LoyaltyTransactionRequest();
        request.setType(LoyaltyTransactionType.DEDUCT);
        request.setPoints(50);
        request.setReason("manual_adjustment");

        assertThatThrownBy(() -> customerService.createLoyaltyTransaction(customer.getId(), request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    void createLoyaltyTransactionUpdatesBalanceAndAuditLog() throws Exception {
        User actor = user(5L, "cashier@pos.com", UserRole.ROLE_BRANCH_CASHIER);
        com.zosh.modal.Store store = new com.zosh.modal.Store();
        store.setId(77L);
        actor.setStore(store);
        Customer customer = new Customer();
        customer.setId(10L);
        customer.setLoyaltyPoints(240);
        customer.setStore(store);

        when(delegatedUserService.getCurrentUser()).thenReturn(actor);
        when(customerRepository.findByStore_IdAndId(store.getId(), customer.getId())).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loyaltyTransactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(invocation -> {
            LoyaltyTransaction tx = invocation.getArgument(0);
            tx.setId(99L);
            tx.setCreatedAt(LocalDateTime.now());
            return tx;
        });

        LoyaltyTransactionRequest request = new LoyaltyTransactionRequest();
        request.setType(LoyaltyTransactionType.ADD);
        request.setPoints(100);
        request.setReason("manual_adjustment");
        request.setNote("Festival bonus");

        LoyaltyTransactionResponse response = customerService.createLoyaltyTransaction(customer.getId(), request);

        assertThat(response.getPointsBefore()).isEqualTo(240);
        assertThat(response.getPointsChanged()).isEqualTo(100);
        assertThat(response.getPointsAfter()).isEqualTo(340);
        assertThat(response.getTransactionId()).isEqualTo(99L);
    }

    private User user(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFullName("Test User");
        user.setRole(role);
        user.setForcePasswordChange(false);
        return user;
    }

    private Branch branch(Long id, String name) {
        Branch branch = new Branch();
        branch.setId(id);
        branch.setName(name);
        return branch;
    }
}
