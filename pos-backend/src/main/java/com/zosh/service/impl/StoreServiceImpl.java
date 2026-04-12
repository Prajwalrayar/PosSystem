package com.zosh.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.domain.StoreStatus;
import com.zosh.domain.UserRole;
import com.zosh.exception.BusinessValidationException;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.exception.UserException;
import com.zosh.mapper.StoreMapper;
import com.zosh.mapper.UserMapper;
import com.zosh.modal.Branch;
import com.zosh.modal.CommissionHistory;
import com.zosh.modal.Store;
import com.zosh.modal.StoreSettings;
import com.zosh.modal.StoreContact;
import com.zosh.modal.User;
import com.zosh.payload.dto.StoreDTO;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.CommissionUpdateRequest;
import com.zosh.payload.request.StoreSettingsRequest;
import com.zosh.payload.response.CommissionResponse;
import com.zosh.payload.response.StoreSettingsResponse;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.CommissionHistoryRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.repository.StoreSettingsRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.StoreService;

import com.zosh.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final UserService userService;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommissionHistoryRepository commissionHistoryRepository;
    private final StoreSettingsRepository storeSettingsRepository;
    private final ObjectMapper objectMapper;


    @Override
    public StoreDTO createStore(StoreDTO storeDto, User user) {
        Store store = StoreMapper.toEntity(storeDto, user);


        return StoreMapper.toDto(storeRepository.save(store));
    }

    @Override
    public StoreDTO getStoreById(Long id) throws ResourceNotFoundException {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        return StoreMapper.toDto(store);
    }

    @Override
    public List<StoreDTO> getAllStores(StoreStatus status) {
        List<Store> stores;
        if (status != null) {
            stores = storeRepository.findByStatus(status);
        } else {
            stores = storeRepository.findAll();
        }

        return stores.stream()
                .map(StoreMapper::toDto)
                .collect(Collectors.toList());


    }

    @Override
    public Store getStoreByAdminId() throws UserException {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getStore() != null) {
            return currentUser.getStore();
        }

        Store store = storeRepository.findByStoreAdminId(currentUser.getId());
        if (store != null) {
            return store;
        }

        throw new UserException("No store assigned to the current user");
    }

    @Override
    public StoreDTO getStoreByEmployee() throws UserException {
        User currentUser=userService.getCurrentUser();


        if(currentUser.getStore()==null){
            throw new UserException("user does not have enough permissions to access this store");
        }
        return StoreMapper.toDto(currentUser.getStore());
    }

    @Override
    public StoreSettingsResponse getStoreSettings(Long storeId) throws UserException {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != UserRole.ROLE_STORE_MANAGER
                && currentUser.getRole() != UserRole.ROLE_STORE_ADMIN
                && currentUser.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You are not allowed to view store settings");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));

        if (currentUser.getRole() != UserRole.ROLE_ADMIN
                && (store.getStoreAdmin() == null || !store.getStoreAdmin().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You can view settings only for your store");
        }

        StoreSettings settings = storeSettingsRepository.findById(storeId).orElse(null);
        if (settings == null) {
            return new StoreSettingsResponse(storeId, null, null, null, null, null);
        }

        return new StoreSettingsResponse(
                storeId,
                readJson(settings.getStoreJson(), StoreSettingsRequest.StoreProfileSettings.class),
                readJson(settings.getNotificationJson(), StoreSettingsRequest.NotificationSettings.class),
                readJson(settings.getSecurityJson(), StoreSettingsRequest.SecuritySettings.class),
                readJson(settings.getPaymentJson(), StoreSettingsRequest.PaymentSettings.class),
                settings.getUpdatedAt()
        );
    }

    @Override
    public StoreDTO updateStore(Long id, StoreDTO storeDto) throws ResourceNotFoundException, UserException {
        User currentUser = userService.getCurrentUser();
        Store existing = currentUser.getStore() != null
                ? currentUser.getStore()
                : storeRepository.findByStoreAdminId(currentUser.getId());

        if(existing == null) {
            throw new ResourceNotFoundException("store not found");
        }

        existing.setBrand(storeDto.getBrand());
        existing.setDescription(storeDto.getDescription());

        // Convert string storeType to enum, if not null
        if (storeDto.getStoreType() != null) {
            existing.setStoreType(storeDto.getStoreType());
        }

        // Set contact info if provided
        if (storeDto.getContact() != null) {
            StoreContact contact = StoreContact.builder()
                    .address(storeDto.getContact().getAddress())
                    .phone(storeDto.getContact().getPhone())
                    .email(storeDto.getContact().getEmail())
                    .build();
            existing.setContact(contact);
        }

        return StoreMapper.toDto(storeRepository.save(existing));
    }

    @Override
    public void deleteStore() throws ResourceNotFoundException, UserException {
        Store store= getStoreByAdminId();

        if (store==null) {
            throw new ResourceNotFoundException("Store not found");
        }
        storeRepository.deleteById(store.getId());
    }

    @Override
    public UserDTO addEmployee(Long id, UserDTO userDto) throws UserException {
        Store store = getStoreByAdminId();

        User employee = UserMapper.toEntity(userDto);
        if(userDto.getRole()== UserRole.ROLE_STORE_MANAGER){
            employee.setStore(store);
        }else if(userDto.getRole()== UserRole.ROLE_BRANCH_MANAGER){
            Branch branch=branchRepository.findById(userDto.getBranchId()).orElseThrow(
                    ()-> new EntityNotFoundException("branch not found")
            );
            employee.setBranch(branch);
            employee.setStore(store);
        }

        employee.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User addedEmployee=userRepository.save(employee);

        return UserMapper.toDTO(addedEmployee);
    }

    @Override
    public List<UserDTO> getEmployeesByStore(Long storeId) throws UserException {
        User currentUser=userService.getCurrentUser();

        Store store=storeRepository.findById(storeId).orElseThrow(
                ()->new EntityNotFoundException("store not found")
        );
        if(currentUser.getRole() == UserRole.ROLE_ADMIN
                || (store.getStoreAdmin() != null && store.getStoreAdmin().getId().equals(currentUser.getId()))
                || (currentUser.getStore() != null && currentUser.getStore().getId().equals(store.getId()))){
            List<User> employees=userRepository.findByStoreId(storeId);
            return UserMapper.toDTOList(employees);
        }

        throw new UserException("user does not have enough permissions to access this store");
    }


    @Override
    public StoreDTO moderateStore(Long storeId, StoreStatus action) throws ResourceNotFoundException {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

      store.setStatus(action);
        Store updatedStore = storeRepository.save(store);
        return StoreMapper.toDto(updatedStore);
    }

    @Override
    public List<CommissionResponse> getCommissionList() {
        User currentUser = userService.getCurrentUser();
        ensureSuperAdmin(currentUser);
        return storeRepository.findAll().stream()
                .map(store -> new CommissionResponse(
                        store.getId(),
                        store.getBrand(),
                        null,
                        defaultRate(store.getCommissionRate()),
                        store.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommissionResponse updateCommission(Long storeId, CommissionUpdateRequest request) throws ResourceNotFoundException {
        User currentUser = userService.getCurrentUser();
        ensureSuperAdmin(currentUser);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        BigDecimal previousRate = defaultRate(store.getCommissionRate());
        BigDecimal nextRate = request.getRate().setScale(2, BigDecimal.ROUND_HALF_UP);
        store.setCommissionRate(nextRate);
        Store savedStore = storeRepository.save(store);

        CommissionHistory history = new CommissionHistory();
        history.setStore(savedStore);
        history.setOldRate(previousRate);
        history.setNewRate(nextRate);
        history.setUpdatedBy(currentUser);
        commissionHistoryRepository.save(history);

        return new CommissionResponse(
                savedStore.getId(),
                savedStore.getBrand(),
                previousRate,
                nextRate,
                savedStore.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public StoreSettingsResponse updateStoreSettings(Long storeId, StoreSettingsRequest request) throws ResourceNotFoundException {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != UserRole.ROLE_STORE_MANAGER
                && currentUser.getRole() != UserRole.ROLE_STORE_ADMIN
                && currentUser.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You are not allowed to update store settings");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        if (currentUser.getRole() != UserRole.ROLE_ADMIN
                && (store.getStoreAdmin() == null || !store.getStoreAdmin().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You can update settings only for your store");
        }

        validateStoreSettingsRequest(request);

        StoreSettings settings = storeSettingsRepository.findById(storeId)
                .orElseGet(() -> {
                    StoreSettings created = new StoreSettings();
                    created.setStore(store);
                    return created;
                });

        if (request.getStoreSettings() != null) {
            settings.setStoreJson(writeJson(request.getStoreSettings()));
        }
        if (request.getNotificationSettings() != null) {
            settings.setNotificationJson(writeJson(request.getNotificationSettings()));
        }
        if (request.getSecuritySettings() != null) {
            settings.setSecurityJson(writeJson(request.getSecuritySettings()));
        }
        if (request.getPaymentSettings() != null) {
            settings.setPaymentJson(writeJson(request.getPaymentSettings()));
        }

        if (request.getStoreSettings() != null) {
            applyStoreProfile(store, request.getStoreSettings());
            storeRepository.save(store);
        }

        StoreSettings saved = storeSettingsRepository.save(settings);

        return new StoreSettingsResponse(
                storeId,
                readJson(saved.getStoreJson(), StoreSettingsRequest.StoreProfileSettings.class),
                readJson(saved.getNotificationJson(), StoreSettingsRequest.NotificationSettings.class),
                readJson(saved.getSecurityJson(), StoreSettingsRequest.SecuritySettings.class),
                readJson(saved.getPaymentJson(), StoreSettingsRequest.PaymentSettings.class),
                saved.getUpdatedAt()
        );
    }

    private void applyStoreProfile(Store store, StoreSettingsRequest.StoreProfileSettings request) {
        if (request.getStoreName() != null) {
            store.setBrand(request.getStoreName().trim());
        }
        if (request.getStoreDescription() != null) {
            store.setDescription(request.getStoreDescription().trim());
        }
        StoreContact contact = store.getContact() == null ? new StoreContact() : store.getContact();
        if (request.getStoreEmail() != null) {
            contact.setEmail(request.getStoreEmail().trim());
        }
        if (request.getStorePhone() != null) {
            contact.setPhone(normalizeNullable(request.getStorePhone()));
        }
        if (request.getStoreAddress() != null) {
            contact.setAddress(request.getStoreAddress().trim());
        }
        store.setContact(contact);
    }

    private void validateStoreSettingsRequest(StoreSettingsRequest request) {
        if (request.getSecuritySettings() != null
                && Boolean.TRUE.equals(request.getSecuritySettings().getIpRestriction())
                && request.getSecuritySettings().getSessionTimeout() == null) {
            throw new BusinessValidationException("securitySettings.sessionTimeout", "Session timeout is required when IP restriction is enabled");
        }
    }

    private void ensureSuperAdmin(User user) {
        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("Only super admin can manage commissions");
        }
    }

    private BigDecimal defaultRate(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize store settings", ex);
        }
    }

    private <T> T readJson(String value, Class<T> type) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize store settings", ex);
        }
    }


}
