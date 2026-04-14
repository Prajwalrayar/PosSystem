package com.zosh.service.impl;


import com.zosh.domain.UserRole;
import com.zosh.exception.BusinessValidationException;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.modal.Customer;
import com.zosh.modal.LoyaltyTransaction;
import com.zosh.modal.LoyaltyTransactionType;
import com.zosh.modal.Store;
import com.zosh.modal.User;
import com.zosh.payload.request.LoyaltyTransactionRequest;
import com.zosh.payload.response.LoyaltyTransactionResponse;
import com.zosh.repository.CustomerRepository;
import com.zosh.repository.LoyaltyTransactionRepository;
import com.zosh.repository.StoreRepository;
import com.zosh.service.CustomerService;
import com.zosh.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;

    @Override
    public Customer createCustomer(Customer customer) {
        Store store = getCurrentStore();
        customer.setStore(store);

        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            String normalizedEmail = customer.getEmail().trim().toLowerCase();
            customer.setEmail(normalizedEmail);

            Customer existing = customerRepository.findByStore_IdAndEmailIgnoreCase(store.getId(), normalizedEmail);
            if (existing != null) {
                existing.setFullName(customer.getFullName());
                existing.setPhone(customer.getPhone());
                existing.setEmail(normalizedEmail);
                existing.setStore(store);
                return customerRepository.save(existing);
            }
        }

        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customerData) throws ResourceNotFoundException {
        Store store = getCurrentStore();
        Customer customer = customerRepository.findByStore_IdAndId(store.getId(), id);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with id " + id);
        }

        customer.setFullName(customerData.getFullName());
        customer.setEmail(customerData.getEmail());
        customer.setPhone(customerData.getPhone());
        customer.setStore(store);

        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            String normalizedEmail = customer.getEmail().trim().toLowerCase();
            Customer existing = customerRepository.findByStore_IdAndEmailIgnoreCase(store.getId(), normalizedEmail);
            if (existing != null && !existing.getId().equals(customer.getId())) {
                throw new BusinessValidationException("email", "Customer email already exists in this store");
            }
            customer.setEmail(normalizedEmail);
        }

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) throws ResourceNotFoundException {
        Store store = getCurrentStore();
        Customer customer = customerRepository.findByStore_IdAndId(store.getId(), id);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with id " + id);
        }
        customerRepository.delete(customer);
    }

    @Override
    public Customer getCustomerById(Long id) throws ResourceNotFoundException {
        Store store = getCurrentStore();
        Customer customer = customerRepository.findByStore_IdAndId(store.getId(), id);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with id " + id);
        }
        return customer;
    }

    @Override
    public List<Customer> getAllCustomers() {
        Store store = getCurrentStore();
        return customerRepository.findByStore_Id(store.getId());
    }

    @Override
    public List<Customer> searchCustomer(String keyword) {
        Store store = getCurrentStore();
        return customerRepository.searchByStoreIdAndKeyword(store.getId(), keyword);
    }

    @Override
    @Transactional
    public LoyaltyTransactionResponse createLoyaltyTransaction(Long customerId, LoyaltyTransactionRequest request) throws ResourceNotFoundException {
        User actor = userService.getCurrentUser();
        if (!(actor.getRole() == UserRole.ROLE_BRANCH_CASHIER
                || actor.getRole() == UserRole.ROLE_BRANCH_ADMIN
                || actor.getRole() == UserRole.ROLE_BRANCH_MANAGER)) {
            throw new AccessDeniedException("You are not allowed to adjust loyalty points");
        }

        Store store = getCurrentStore();
        Customer customer = customerRepository.findByStore_IdAndId(store.getId(), customerId);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with id " + customerId);
        }
        int pointsBefore = customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints();
        int delta = request.getType() == LoyaltyTransactionType.DEDUCT ? -request.getPoints() : request.getPoints();
        int pointsAfter = pointsBefore + delta;
        if (pointsAfter < 0) {
            throw new BusinessValidationException("points", "Resulting loyalty balance cannot be negative");
        }

        customer.setLoyaltyPoints(pointsAfter);
        customerRepository.save(customer);

        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setCustomer(customer);
        transaction.setType(request.getType());
        transaction.setPoints(request.getPoints());
        transaction.setReason(request.getReason().trim());
        transaction.setNote(request.getNote());
        transaction.setCreatedBy(actor);
        LoyaltyTransaction savedTransaction = loyaltyTransactionRepository.save(transaction);

        return new LoyaltyTransactionResponse(
                customer.getId(),
                pointsBefore,
                delta,
                pointsAfter,
                savedTransaction.getId()
        );
    }

    private Store getCurrentStore() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getStore() != null) {
            return currentUser.getStore();
        }

        if (currentUser.getBranch() != null && currentUser.getBranch().getStore() != null) {
            return currentUser.getBranch().getStore();
        }

        Store store = storeRepository.findByStoreAdminId(currentUser.getId());
        if (store != null) {
            return store;
        }

        throw new AccessDeniedException("No store assigned to the current user");
    }

}
