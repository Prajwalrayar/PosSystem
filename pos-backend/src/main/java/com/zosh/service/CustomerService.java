package com.zosh.service;


import com.zosh.exception.ResourceNotFoundException;
import com.zosh.modal.Customer;
import com.zosh.payload.request.LoyaltyTransactionRequest;
import com.zosh.payload.response.LoyaltyTransactionResponse;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer updateCustomer(Long id, Customer customer) throws ResourceNotFoundException;

    void deleteCustomer(Long id) throws ResourceNotFoundException;

    Customer getCustomerById(Long id) throws ResourceNotFoundException;

    List<Customer> getAllCustomers();

    List<Customer> searchCustomer(String keyword);

    LoyaltyTransactionResponse createLoyaltyTransaction(Long customerId, LoyaltyTransactionRequest request) throws ResourceNotFoundException;

}

