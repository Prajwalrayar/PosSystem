package com.zosh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.domain.StoreStatus;
import com.zosh.domain.UserRole;
import com.zosh.exception.BusinessValidationException;
import com.zosh.modal.Branch;
import com.zosh.modal.BranchSettings;
import com.zosh.modal.CommissionHistory;
import com.zosh.modal.Store;
import com.zosh.modal.StoreContact;
import com.zosh.modal.StoreSettings;
import com.zosh.modal.User;
import com.zosh.payload.dto.StoreDTO;
import com.zosh.payload.request.BranchSettingsRequest;
import com.zosh.payload.request.CommissionUpdateRequest;
import com.zosh.payload.request.StoreSettingsRequest;
import com.zosh.payload.response.BranchSettingsResponse;
import com.zosh.payload.response.CommissionResponse;
import com.zosh.payload.response.StoreSettingsResponse;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.BranchSettingsRepository;
import com.zosh.repository.CommissionHistoryRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.repository.StoreSettingsRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.UserService;
import com.zosh.service.impl.BranchServiceImpl;
import com.zosh.service.impl.StoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class P1ServiceTests {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private CommissionHistoryRepository commissionHistoryRepository;
    @Mock
    private StoreSettingsRepository storeSettingsRepository;

    private StoreServiceImpl storeService;

    @Mock
    private BranchSettingsRepository branchSettingsRepository;

    private BranchServiceImpl branchService;

    @BeforeEach
    void setUp() {
        storeService = new StoreServiceImpl(
                storeRepository,
                userService,
                branchRepository,
                userRepository,
                new BCryptPasswordEncoder(),
                commissionHistoryRepository,
                storeSettingsRepository,
                new ObjectMapper()
        );
        branchService = new BranchServiceImpl(
                branchRepository,
                storeRepository,
                userRepository,
                new BCryptPasswordEncoder(),
                userService,
                branchSettingsRepository,
                new ObjectMapper()
        );
    }

    @Test
    void updateCommissionStoresNewRateAndHistory() throws Exception {
        User admin = user(1L, UserRole.ROLE_ADMIN);
        Store store = store(5L);
        store.setCommissionRate(new BigDecimal("2.50"));

        when(userService.getCurrentUser()).thenReturn(admin);
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commissionHistoryRepository.save(any(CommissionHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommissionUpdateRequest request = new CommissionUpdateRequest();
        request.setRate(new BigDecimal("2.75"));

        CommissionResponse response = storeService.updateCommission(store.getId(), request);

        assertThat(response.getPreviousRate()).isEqualByComparingTo("2.50");
        assertThat(response.getCurrentRate()).isEqualByComparingTo("2.75");
        verify(commissionHistoryRepository).save(any(CommissionHistory.class));
    }

    @Test
    void updateBranchSettingsRequiresUpiIdWhenUpiEnabled() {
        User branchAdmin = user(2L, UserRole.ROLE_BRANCH_ADMIN);
        Branch branch = branch(8L);
        branchAdmin.setBranch(branch);

        when(userService.getCurrentUser()).thenReturn(branchAdmin);
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));

        BranchSettingsRequest request = new BranchSettingsRequest();
        BranchSettingsRequest.PaymentSettings payment = new BranchSettingsRequest.PaymentSettings();
        payment.setAcceptUPI(true);
        payment.setAcceptCash(true);
        request.setPayment(payment);

        assertThatThrownBy(() -> branchService.updateBranchSettings(branch.getId(), request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("UPI ID");
    }

    @Test
    void updateBranchSettingsPersistsJsonSections() {
        User branchAdmin = user(2L, UserRole.ROLE_BRANCH_ADMIN);
        Branch branch = branch(8L);
        branchAdmin.setBranch(branch);

        when(userService.getCurrentUser()).thenReturn(branchAdmin);
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(branchSettingsRepository.findById(branch.getId())).thenReturn(Optional.empty());
        when(branchSettingsRepository.save(any(BranchSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BranchSettingsRequest request = new BranchSettingsRequest();
        BranchSettingsRequest.PaymentSettings payment = new BranchSettingsRequest.PaymentSettings();
        payment.setAcceptCash(true);
        payment.setAcceptUPI(true);
        payment.setUpiId("store@upi");
        request.setPayment(payment);

        BranchSettingsResponse response = branchService.updateBranchSettings(branch.getId(), request);

        assertThat(response.getBranchId()).isEqualTo(branch.getId());
        assertThat(response.getPayment().getUpiId()).isEqualTo("store@upi");
    }

    @Test
    void updateStoreSettingsPersistsProfileAndJson() throws Exception {
        User storeAdmin = user(3L, UserRole.ROLE_STORE_MANAGER);
        Store store = store(9L);
        storeAdmin.setStore(store);
        store.setStoreAdmin(storeAdmin);
        store.setContact(new StoreContact());

        when(userService.getCurrentUser()).thenReturn(storeAdmin);
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(storeSettingsRepository.findById(store.getId())).thenReturn(Optional.empty());
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(storeSettingsRepository.save(any(StoreSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoreSettingsRequest request = new StoreSettingsRequest();
        StoreSettingsRequest.StoreProfileSettings profile = new StoreSettingsRequest.StoreProfileSettings();
        profile.setStoreName("Updated Store");
        profile.setStoreEmail("store@example.com");
        profile.setStorePhone("+12345678901");
        profile.setStoreAddress("Address");
        request.setStoreSettings(profile);

        StoreSettingsRequest.SecuritySettings security = new StoreSettingsRequest.SecuritySettings();
        security.setTwoFactorAuth(false);
        security.setSessionTimeout(30);
        security.setIpRestriction(false);
        request.setSecuritySettings(security);

        StoreSettingsResponse response = storeService.updateStoreSettings(store.getId(), request);

        assertThat(response.getStoreId()).isEqualTo(store.getId());
        assertThat(store.getBrand()).isEqualTo("Updated Store");
        assertThat(store.getContact().getEmail()).isEqualTo("store@example.com");
    }

    @Test
    void createStoreDefaultsCommissionRateWhenRequestOmitsIt() {
        User storeAdmin = user(3L, UserRole.ROLE_STORE_MANAGER);
        StoreDTO request = StoreDTO.builder()
                .brand("Fresh Mart")
                .description("Retail")
                .build();

        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoreDTO response = storeService.createStore(request, storeAdmin);

        assertThat(response.getCommissionRate()).isEqualByComparingTo("0");
    }

    @Test
    void getCommissionListReturnsAllStoresForSuperAdmin() {
        User admin = user(1L, UserRole.ROLE_ADMIN);
        when(userService.getCurrentUser()).thenReturn(admin);
        when(storeRepository.findAll()).thenReturn(List.of(store(1L), store(2L)));

        List<CommissionResponse> response = storeService.getCommissionList();

        assertThat(response).hasSize(2);
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
        store.setStatus(StoreStatus.ACTIVE);
        store.setCommissionRate(new BigDecimal("1.50"));
        return store;
    }

    private Branch branch(Long id) {
        Branch branch = new Branch();
        branch.setId(id);
        branch.setName("Branch " + id);
        return branch;
    }
}
