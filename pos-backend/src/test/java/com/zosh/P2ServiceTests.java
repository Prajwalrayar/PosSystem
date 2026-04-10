package com.zosh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.domain.OrderStatus;
import com.zosh.domain.PaymentType;
import com.zosh.domain.UserRole;
import com.zosh.modal.Branch;
import com.zosh.modal.Category;
import com.zosh.modal.ExportJob;
import com.zosh.modal.ExportJobStatus;
import com.zosh.modal.Order;
import com.zosh.modal.OrderItem;
import com.zosh.modal.PrintJob;
import com.zosh.modal.PrintJobStatus;
import com.zosh.modal.Product;
import com.zosh.modal.Refund;
import com.zosh.modal.ReturnSession;
import com.zosh.modal.ShiftReport;
import com.zosh.modal.Store;
import com.zosh.modal.User;
import com.zosh.payload.request.CreateExportRequest;
import com.zosh.payload.request.CreatePrintJobRequest;
import com.zosh.payload.request.InitiateReturnRequest;
import com.zosh.payload.response.EmployeePerformanceResponse;
import com.zosh.payload.response.ExportJobResponse;
import com.zosh.payload.response.PrintJobResponse;
import com.zosh.payload.response.ReturnInitiationResponse;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.ExportJobRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PrintJobRepository;
import com.zosh.repository.RefundRepository;
import com.zosh.repository.ReturnSessionRepository;
import com.zosh.repository.ShiftReportRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.UserService;
import com.zosh.service.impl.EmployeeServiceImpl;
import com.zosh.service.impl.ExportServiceImpl;
import com.zosh.service.impl.PrintJobServiceImpl;
import com.zosh.service.impl.ReturnServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class P2ServiceTests {

    @Mock
    private ExportJobRepository exportJobRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserService userService;

    private ExportServiceImpl exportService;

    @Mock
    private PrintJobRepository printJobRepository;
    @Mock
    private ShiftReportRepository shiftReportRepository;

    private PrintJobServiceImpl printJobService;

    @Mock
    private ReturnSessionRepository returnSessionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BranchRepository branchRepository;

    private ReturnServiceImpl returnService;

    @Mock
    private com.zosh.repository.CustomerRepository customerRepository;
    @Mock
    private com.zosh.repository.LoyaltyTransactionRepository loyaltyTransactionRepository;
    @Mock
    private com.zosh.repository.StoreRepository unusedStoreRepository;
    @Mock
    private com.zosh.repository.StoreSettingsRepository storeSettingsRepository;
    @Mock
    private com.zosh.repository.CommissionHistoryRepository commissionHistoryRepository;
    @Mock
    private com.zosh.repository.BranchSettingsRepository branchSettingsRepository;
    @Mock
    private RefundRepository refundRepository;

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        exportService = new ExportServiceImpl(
                exportJobRepository,
                storeRepository,
                orderRepository,
                userService,
                new ObjectMapper()
        );
        printJobService = new PrintJobServiceImpl(
                printJobRepository,
                orderRepository,
                shiftReportRepository,
                userService
        );
        returnService = new ReturnServiceImpl(
                returnSessionRepository,
                orderRepository,
                userRepository,
                branchRepository,
                userService,
                new ObjectMapper()
        );
        employeeService = new EmployeeServiceImpl(
                userRepository,
                storeRepository,
                branchRepository,
                exportJobRepository,
                printJobRepository,
                loyaltyTransactionRepository,
                new BCryptPasswordEncoder(),
                userService,
                orderRepository,
                refundRepository,
                shiftReportRepository
        );
    }

    @Test
    void createExportCreatesCompletedJobAndDownloadableContent() {
        User admin = user(1L, UserRole.ROLE_ADMIN);
        AtomicReference<ExportJob> savedRef = new AtomicReference<>();
        when(userService.getCurrentUser()).thenReturn(admin);
        when(storeRepository.findAll()).thenReturn(List.of(store(10L)));
        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(invocation -> {
            ExportJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(UUID.randomUUID());
            }
            savedRef.set(job);
            return job;
        });
        when(exportJobRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.ofNullable(savedRef.get()));

        CreateExportRequest request = new CreateExportRequest();
        request.setType("STORE_LIST");
        request.setFormat("CSV");
        request.setFilters(Map.of("from", "2026-04-01"));

        ExportJobResponse response = exportService.createExport(request);

        assertThat(response.getStatus()).isEqualTo(ExportJobStatus.COMPLETED);
        assertThat(response.getProgress()).isEqualTo(100);
        assertThat(new String(exportService.downloadExport(response.getId()))).contains("storeId,brand,status,commissionRate");
    }

    @Test
    void createPrintJobCompletesInvoiceJob() {
        User cashier = user(2L, UserRole.ROLE_BRANCH_CASHIER);
        cashier.setBranch(branch(7L));
        Order order = order(15L, cashier, cashier.getBranch(), OrderStatus.COMPLETED);

        when(userService.getCurrentUser()).thenReturn(cashier);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(printJobRepository.save(any(PrintJob.class))).thenAnswer(invocation -> {
            PrintJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(UUID.randomUUID());
            }
            return job;
        });

        CreatePrintJobRequest request = new CreatePrintJobRequest();
        request.setType("INVOICE");
        request.setReferenceId(order.getId().toString());
        request.setPrinterId("branch_printer_1");

        PrintJobResponse response = printJobService.createPrintJob(request);

        assertThat(response.getStatus()).isEqualTo(PrintJobStatus.COMPLETED);
        assertThat(response.getType()).isEqualTo("INVOICE");
    }

    @Test
    void initiateReturnCreatesSessionWithEligibleItems() throws Exception {
        User cashier = user(3L, UserRole.ROLE_BRANCH_CASHIER);
        Branch branch = branch(7L);
        cashier.setBranch(branch);
        Order order = order(20L, cashier, branch, OrderStatus.COMPLETED);
        Product product = new Product();
        product.setId(101L);
        product.setName("Milk");
        product.setSku("SKU-1");
        product.setMrp(150D);
        product.setSellingPrice(150D);
        product.setStore(store(1L));
        Category category = new Category();
        category.setId(201L);
        category.setName("Groceries");
        product.setCategory(category);
        OrderItem item = OrderItem.builder().id(31L).product(product).quantity(2).price(300D).order(order).build();
        order.setItems(List.of(item));

        when(userService.getCurrentUser()).thenReturn(cashier);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findById(cashier.getId())).thenReturn(Optional.of(cashier));
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(returnSessionRepository.save(any(ReturnSession.class))).thenAnswer(invocation -> {
            ReturnSession session = invocation.getArgument(0);
            session.setId(UUID.randomUUID());
            return session;
        });

        InitiateReturnRequest request = new InitiateReturnRequest();
        request.setOrderId(order.getId());
        request.setCashierId(cashier.getId());
        request.setBranchId(branch.getId());

        ReturnInitiationResponse response = returnService.initiateReturn(request);

        assertThat(response.getReturnSessionId()).isNotNull();
        assertThat(response.getEligibleItems()).hasSize(1);
        assertThat(response.getEligibleItems().get(0).getProductId()).isEqualTo(101L);
    }

    @Test
    void getEmployeePerformanceBuildsSummaryFromOrdersRefundsAndShifts() throws Exception {
        User actor = user(4L, UserRole.ROLE_BRANCH_ADMIN);
        Branch branch = branch(9L);
        actor.setBranch(branch);

        User employee = user(5L, UserRole.ROLE_BRANCH_CASHIER);
        employee.setBranch(branch);

        Order firstOrder = order(1L, employee, branch, OrderStatus.COMPLETED);
        firstOrder.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        firstOrder.setTotalAmount(500D);
        Order secondOrder = order(2L, employee, branch, OrderStatus.COMPLETED);
        secondOrder.setCreatedAt(LocalDateTime.of(2026, 3, 2, 11, 0));
        secondOrder.setTotalAmount(700D);

        Refund refund = new Refund();
        refund.setCreatedAt(LocalDateTime.of(2026, 3, 2, 12, 0));
        refund.setOrder(secondOrder);
        refund.setAmount(50D);

        ShiftReport shift = new ShiftReport();
        shift.setShiftStart(LocalDateTime.of(2026, 3, 1, 9, 0));
        shift.setBranch(branch);

        when(userService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(orderRepository.findByCashierAndCreatedAtBetween(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(firstOrder, secondOrder));
        when(refundRepository.findByCashierAndCreatedAtBetween(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(refund));
        when(shiftReportRepository.findByCashier(employee)).thenReturn(List.of(shift));

        EmployeePerformanceResponse response = employeeService.getEmployeePerformance(
                employee.getId(),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        assertThat(response.getOrdersProcessed()).isEqualTo(2);
        assertThat(response.getTotalSales()).isEqualTo(1200D);
        assertThat(response.getAverageOrderValue()).isEqualTo(600D);
        assertThat(response.getDailySales()).hasSize(2);
        assertThat(response.getActivityLog()).isNotEmpty();
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@pos.com");
        user.setFullName("User " + id);
        user.setRole(role);
        return user;
    }

    private Store store(Long id) {
        Store store = new Store();
        store.setId(id);
        store.setBrand("Store " + id);
        return store;
    }

    private Branch branch(Long id) {
        Branch branch = new Branch();
        branch.setId(id);
        branch.setName("Branch " + id);
        return branch;
    }

    private Order order(Long id, User cashier, Branch branch, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setCashier(cashier);
        order.setBranch(branch);
        order.setStatus(status);
        order.setPaymentType(PaymentType.CASH);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(100D);
        return order;
    }
}
