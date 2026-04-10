package com.zosh.service.impl;


import com.zosh.domain.UserRole;
import com.zosh.exception.BusinessValidationException;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.modal.Customer;
import com.zosh.modal.LoyaltyTransaction;
import com.zosh.modal.LoyaltyTransactionType;
import com.zosh.modal.User;
import com.zosh.payload.request.LoyaltyTransactionRequest;
import com.zosh.payload.response.LoyaltyTransactionResponse;
import com.zosh.repository.CustomerRepository;
import com.zosh.repository.LoyaltyTransactionRepository;
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
    private final UserService userService;

    @Override
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customerData) throws ResourceNotFoundException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer not found with id " + id));

        customer.setFullName(customerData.getFullName());
        customer.setEmail(customerData.getEmail());
        customer.setPhone(customerData.getPhone());

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) throws ResourceNotFoundException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
        customerRepository.delete(customer);
    }

    @Override
    public Customer getCustomerById(Long id) throws ResourceNotFoundException {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> searchCustomer(String keyword) {
        return customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
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

        Customer customer = getCustomerById(customerId);
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

}
