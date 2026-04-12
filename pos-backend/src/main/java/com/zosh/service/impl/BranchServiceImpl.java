package com.zosh.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zosh.domain.UserRole;
import com.zosh.exception.BusinessValidationException;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.exception.UserException;
import com.zosh.mapper.BranchMapper;
import com.zosh.mapper.UserMapper;
import com.zosh.modal.Branch;
import com.zosh.modal.BranchSettings;
import com.zosh.modal.Store;
import com.zosh.modal.User;
import com.zosh.payload.dto.BranchDTO;
import com.zosh.payload.request.BranchSettingsRequest;
import com.zosh.payload.response.BranchSettingsResponse;
import com.zosh.payload.dto.UserDTO;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.BranchSettingsRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.BranchService;
import com.zosh.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final BranchSettingsRepository branchSettingsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public BranchDTO createBranch(BranchDTO branchDto, User user) {
        Store store = user.getStore() != null ? user.getStore() : storeRepository.findByStoreAdminId(user.getId());

        Branch branch = BranchMapper.toEntity(branchDto, store);
        return BranchMapper.toDto(branchRepository.save(branch));
    }

    @Override
    public BranchDTO getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
        return BranchMapper.toDto(branch);
    }

    @Override
    public List<BranchDTO> getAllBranchesByStoreId(Long storeId) throws UserException {
        User currentUser=userService.getCurrentUser();
        Store store=storeRepository.findById(storeId).orElseThrow(
                () -> new EntityNotFoundException("Store not found")
        );

        // Check if current user is allowed
        boolean isStoreManager = currentUser.getRole() == UserRole.ROLE_STORE_MANAGER &&
                currentUser.getStore() != null &&
                currentUser.getStore().getId().equals(storeId);

        boolean isStoreAdmin = currentUser.getRole() == UserRole.ROLE_STORE_ADMIN &&
                store.getStoreAdmin() != null &&
                store.getStoreAdmin().getId().equals(currentUser.getId());

        if (!isStoreManager && !isStoreAdmin) {
            throw new UserException("You are not authorized to access this store's branches");
        }

        return branchRepository.findByStoreId(store.getId()).stream()
                .map(BranchMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BranchDTO updateBranch(Long id, BranchDTO branchDto, User user) throws Exception {

//        Store store = storeRepository.findByStoreAdminId(user.getId());

        Branch existing = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

//        if(!store.getId().equals(existing.getStore().getId())){
//            throw new Exception("can't have permission");
//        }

        existing.setName(branchDto.getName());
        existing.setAddress(branchDto.getAddress());
        existing.setEmail(branchDto.getEmail());
        existing.setPhone(branchDto.getPhone());
        existing.setCloseTime(branchDto.getCloseTime());
        existing.setOpenTime(branchDto.getOpenTime());
        existing.setWorkingDays(branchDto.getWorkingDays());

        return BranchMapper.toDto(branchRepository.save(existing));
    }

    @Override
    public BranchSettingsResponse getBranchSettings(Long branchId) {
        User currentUser = userService.getCurrentUser();
        if (!(currentUser.getRole() == UserRole.ROLE_BRANCH_ADMIN
                || currentUser.getRole() == UserRole.ROLE_BRANCH_MANAGER
                || currentUser.getRole() == UserRole.ROLE_BRANCH_CASHIER)) {
            throw new AccessDeniedException("You are not allowed to view branch settings");
        }
        if (currentUser.getBranch() == null || !currentUser.getBranch().getId().equals(branchId)) {
            throw new AccessDeniedException("You can view settings only for your branch");
        }

        BranchSettings settings = branchSettingsRepository.findById(branchId).orElse(null);
        if (settings == null) {
            return new BranchSettingsResponse(branchId, null, null, null, null, null);
        }

        return new BranchSettingsResponse(
                branchId,
                readJson(settings.getPrinterJson(), BranchSettingsRequest.PrinterSettings.class),
                readJson(settings.getTaxJson(), BranchSettingsRequest.TaxSettings.class),
                readJson(settings.getPaymentJson(), BranchSettingsRequest.PaymentSettings.class),
                readJson(settings.getDiscountJson(), BranchSettingsRequest.DiscountSettings.class),
                settings.getUpdatedAt()
        );
    }

    @Override
    public void deleteBranch(Long id) {
        if (!branchRepository.existsById(id)) {
            throw new EntityNotFoundException("Branch not found");
        }
        branchRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BranchSettingsResponse updateBranchSettings(Long branchId, BranchSettingsRequest request) {
        User currentUser = userService.getCurrentUser();
        if (!(currentUser.getRole() == UserRole.ROLE_BRANCH_ADMIN || currentUser.getRole() == UserRole.ROLE_BRANCH_MANAGER)) {
            throw new AccessDeniedException("You are not allowed to update branch settings");
        }
        if (currentUser.getBranch() == null || !currentUser.getBranch().getId().equals(branchId)) {
            throw new AccessDeniedException("You can update settings only for your branch");
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        validateBranchSettingsRequest(request);

        BranchSettings settings = branchSettingsRepository.findById(branchId)
                .orElseGet(() -> {
                    BranchSettings created = new BranchSettings();
                    created.setBranch(branch);
                    return created;
                });

        if (request.getPrinter() != null) {
            settings.setPrinterJson(writeJson(request.getPrinter()));
        }
        if (request.getTax() != null) {
            settings.setTaxJson(writeJson(request.getTax()));
        }
        if (request.getPayment() != null) {
            settings.setPaymentJson(writeJson(request.getPayment()));
        }
        if (request.getDiscount() != null) {
            settings.setDiscountJson(writeJson(request.getDiscount()));
        }

        BranchSettings saved = branchSettingsRepository.save(settings);
        return new BranchSettingsResponse(
                branchId,
                readJson(saved.getPrinterJson(), BranchSettingsRequest.PrinterSettings.class),
                readJson(saved.getTaxJson(), BranchSettingsRequest.TaxSettings.class),
                readJson(saved.getPaymentJson(), BranchSettingsRequest.PaymentSettings.class),
                readJson(saved.getDiscountJson(), BranchSettingsRequest.DiscountSettings.class),
                saved.getUpdatedAt()
        );
    }

    private void validateBranchSettingsRequest(BranchSettingsRequest request) {
        if (request.getTax() != null
                && Boolean.TRUE.equals(request.getTax().getGstEnabled())
                && request.getTax().getGstPercentage() == null) {
            throw new BusinessValidationException("tax.gstPercentage", "GST percentage is required when GST is enabled");
        }
        if (request.getPayment() != null
                && Boolean.TRUE.equals(request.getPayment().getAcceptUPI())
                && isBlank(request.getPayment().getUpiId())) {
            throw new BusinessValidationException("payment.upiId", "UPI ID is required when UPI is enabled");
        }
        if (request.getPayment() != null
                && Boolean.TRUE.equals(request.getPayment().getAcceptCard())
                && isBlank(request.getPayment().getCardTerminalId())) {
            throw new BusinessValidationException("payment.cardTerminalId", "Card terminal ID is required when card is enabled");
        }
        if (request.getDiscount() != null
                && Boolean.TRUE.equals(request.getDiscount().getAllowDiscount())
                && request.getDiscount().getMaxDiscountPercentage() == null) {
            throw new BusinessValidationException("discount.maxDiscountPercentage", "Max discount percentage is required when discounts are enabled");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize branch settings", ex);
        }
    }

    private <T> T readJson(String value, Class<T> type) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize branch settings", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
