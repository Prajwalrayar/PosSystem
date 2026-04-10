package com.zosh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.domain.OrderStatus;
import com.zosh.domain.PaymentType;
import com.zosh.domain.StoreStatus;
import com.zosh.domain.UserRole;
import com.zosh.modal.Branch;
import com.zosh.modal.Category;
import com.zosh.modal.Order;
import com.zosh.modal.OrderItem;
import com.zosh.modal.Product;
import com.zosh.modal.Refund;
import com.zosh.modal.ShiftReport;
import com.zosh.modal.Store;
import com.zosh.modal.User;
import com.zosh.payload.dto.BranchDTO;
import com.zosh.payload.dto.OrderDTO;
import com.zosh.payload.dto.OrderItemDTO;
import com.zosh.payload.dto.RefundDTO;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.BranchSettingsRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PasswordResetTokenRepository;
import com.zosh.repository.ProductRepository;
import com.zosh.repository.RefundRepository;
import com.zosh.repository.ShiftReportRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.repository.StoreSettingsRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.UserService;
import com.zosh.service.impl.BranchServiceImpl;
import com.zosh.service.impl.OrderServiceImpl;
import com.zosh.service.impl.RefundServiceImpl;
import com.zosh.service.impl.ShiftReportServiceImpl;
import com.zosh.service.impl.UserServiceImpl;
import com.zosh.configrations.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreServiceSmokeTests {

    @Mock private BranchRepository branchRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private RefundRepository refundRepository;
    @Mock private ShiftReportRepository shiftReportRepository;
    @Mock private BranchSettingsRepository branchSettingsRepository;
    @Mock private StoreSettingsRepository storeSettingsRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private JwtProvider jwtProvider;

    @Test
    void branchServiceCreatesBranchForStoreAdmin() {
        UserService userService = new UserServiceImpl(userRepository, new BCryptPasswordEncoder(), jwtProvider, passwordResetTokenRepository);
        BranchServiceImpl branchService = new BranchServiceImpl(
                branchRepository,
                storeRepository,
                userRepository,
                new BCryptPasswordEncoder(),
                userService,
                branchSettingsRepository,
                new ObjectMapper()
        );

        User admin = user(1L, UserRole.ROLE_STORE_ADMIN);
        Store store = store(10L, admin);
        when(storeRepository.findByStoreAdminId(admin.getId())).thenReturn(store);
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> {
            Branch branch = invocation.getArgument(0);
            branch.setId(100L);
            return branch;
        });

        BranchDTO request = BranchDTO.builder().name("Main Branch").address("Address").phone("9999999999").build();
        BranchDTO response = branchService.createBranch(request, admin);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Main Branch");
        assertThat(response.getStoreId()).isEqualTo(10L);
    }

    @Test
    void orderServiceCreatesOrderAndCalculatesTotal() throws Exception {
        UserService userService = org.mockito.Mockito.mock(UserService.class);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, productRepository, branchRepository, userService);

        Branch branch = branch(20L, "Branch");
        User cashier = user(2L, UserRole.ROLE_BRANCH_CASHIER);
        cashier.setBranch(branch);
        when(userService.getCurrentUser()).thenReturn(cashier);

        Product first = product(1L, "Item 1", 50D);
        Product second = product(2L, "Item 2", 30D);
        when(productRepository.findById(1L)).thenReturn(Optional.of(first));
        when(productRepository.findById(2L)).thenReturn(Optional.of(second));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(500L);
            return order;
        });

        OrderDTO request = OrderDTO.builder()
                .paymentType(PaymentType.CASH)
                .items(List.of(
                        OrderItemDTO.builder().productId(1L).quantity(2).build(),
                        OrderItemDTO.builder().productId(2L).quantity(1).build()
                ))
                .build();

        OrderDTO response = orderService.createOrder(request);

        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getTotalAmount()).isEqualTo(130D);
        assertThat(response.getItems()).hasSize(2);
    }

    @Test
    void refundServiceCreatesRefundAndMarksOrderRefunded() throws Exception {
        UserService userService = org.mockito.Mockito.mock(UserService.class);
        RefundServiceImpl refundService = new RefundServiceImpl(refundRepository, orderRepository, userService, branchRepository);

        Branch branch = branch(30L, "Branch");
        User cashier = user(3L, UserRole.ROLE_BRANCH_CASHIER);
        cashier.setBranch(branch);
        Order order = order(700L, cashier, branch);
        order.setTotalAmount(250D);

        when(userService.getCurrentUser()).thenReturn(cashier);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundDTO request = new RefundDTO();
        request.setOrderId(order.getId());
        request.setBranchId(branch.getId());
        request.setReason("Damaged");

        Refund refund = refundService.createRefund(request);

        assertThat(refund.getAmount()).isEqualTo(250D);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    void shiftReportServiceEndsShiftAndBuildsSummary() throws Exception {
        UserService userService = org.mockito.Mockito.mock(UserService.class);
        ShiftReportServiceImpl shiftReportService = new ShiftReportServiceImpl(
                shiftReportRepository,
                userRepository,
                branchRepository,
                orderRepository,
                refundRepository,
                userService
        );

        Branch branch = branch(40L, "Branch");
        User cashier = user(4L, UserRole.ROLE_BRANCH_CASHIER);
        cashier.setBranch(branch);

        ShiftReport openShift = new ShiftReport();
        openShift.setId(900L);
        openShift.setCashier(cashier);
        openShift.setBranch(branch);
        openShift.setShiftStart(LocalDateTime.of(2026, 4, 8, 9, 0));

        Order first = order(1L, cashier, branch);
        first.setCreatedAt(LocalDateTime.of(2026, 4, 8, 10, 0));
        first.setTotalAmount(100D);
        first.setItems(List.of(OrderItem.builder().product(product(9L, "Milk", 25D)).quantity(2).price(50D).order(first).build()));
        Order second = order(2L, cashier, branch);
        second.setCreatedAt(LocalDateTime.of(2026, 4, 8, 11, 0));
        second.setTotalAmount(200D);
        second.setPaymentType(PaymentType.CARD);
        second.setItems(List.of(OrderItem.builder().product(product(10L, "Bread", 40D)).quantity(5).price(200D).order(second).build()));

        Refund refund = new Refund();
        refund.setAmount(25D);
        refund.setCreatedAt(LocalDateTime.of(2026, 4, 8, 11, 30));

        when(userService.getCurrentUser()).thenReturn(cashier);
        when(shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier)).thenReturn(Optional.of(openShift));
        when(orderRepository.findByCashierAndCreatedAtBetween(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(first, second));
        when(refundRepository.findByCashierAndCreatedAtBetween(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(refund));
        when(shiftReportRepository.save(any(ShiftReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShiftReport result = shiftReportService.endShift(null, LocalDateTime.of(2026, 4, 8, 18, 0));

        assertThat(result.getTotalSales()).isEqualTo(300D);
        assertThat(result.getTotalRefunds()).isEqualTo(25D);
        assertThat(result.getNetSales()).isEqualTo(275D);
        assertThat(result.getPaymentSummaries()).hasSize(2);
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@pos.com");
        user.setFullName("User " + id);
        user.setRole(role);
        return user;
    }

    private Store store(Long id, User admin) {
        Store store = new Store();
        store.setId(id);
        store.setBrand("Store " + id);
        store.setStoreAdmin(admin);
        store.setStatus(StoreStatus.ACTIVE);
        return store;
    }

    private Branch branch(Long id, String name) {
        Branch branch = new Branch();
        branch.setId(id);
        branch.setName(name);
        return branch;
    }

    private Product product(Long id, String name, Double sellingPrice) {
        Category category = new Category();
        category.setId(id + 100);
        category.setName("Category " + id);
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSku("SKU-" + id);
        product.setMrp(sellingPrice);
        product.setSellingPrice(sellingPrice);
        product.setCategory(category);
        product.setStore(store(1L, user(99L, UserRole.ROLE_STORE_ADMIN)));
        return product;
    }

    private Order order(Long id, User cashier, Branch branch) {
        Order order = new Order();
        order.setId(id);
        order.setCashier(cashier);
        order.setBranch(branch);
        order.setPaymentType(PaymentType.CASH);
        order.setStatus(OrderStatus.COMPLETED);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
