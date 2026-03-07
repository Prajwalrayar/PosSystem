package com.capstone.service.impl;

import com.capstone.model.Customer;
import com.capstone.repository.CustomerRepository;
import com.capstone.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) throws Exception {
        Customer cust =  customerRepository.findById(id).orElseThrow(
                ()-> new Exception("Customer does not Exist...")
        );
        customer.setFullName(customer.getFullName());
        customer.setEmail(customer.getEmail());
        customer.setPhone(customer.getPhone());

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) throws Exception {

        Customer cust =  customerRepository.findById(id).orElseThrow(
                ()-> new Exception("Customer does not Exist...")
        );
        customerRepository.delete(cust);
    }

    @Override
    public Customer getCustomer(Long id) throws Exception {
        return customerRepository.findById(id).orElseThrow(
                ()-> new Exception("Customer does not Exist...")
        );
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }
}
