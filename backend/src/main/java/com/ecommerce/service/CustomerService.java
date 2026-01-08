package com.ecommerce.service;

import com.ecommerce.dto.CustomerDto;
import com.ecommerce.entity.Customer;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new customer
     */
    public CustomerDto.Response createCustomer(CustomerDto.CreateRequest request) {
        String traceId = MDC.get("traceId");
        log.info("[{}] SERVICE: Creating new customer with email: {}", traceId, request.getEmail());
        log.debug("[{}] SERVICE: Customer details - Name: {}", traceId, request.getName());

        try {
            // Check if email already exists
            log.debug("[{}] SERVICE: Checking if email already exists: {}", traceId, request.getEmail());
            if (customerRepository.existsByEmail(request.getEmail())) {
                log.error("[{}] SERVICE: Customer with email {} already exists", traceId, request.getEmail());
                throw new DuplicateResourceException("Customer with email " + request.getEmail() + " already exists");
            }
            
            log.debug("[{}] SERVICE: Email is available, creating customer entity", traceId);
            Customer customer = Customer.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .build();

            log.debug("[{}] SERVICE: Saving customer to database", traceId);
            Customer savedCustomer = customerRepository.save(customer);
            
            log.info("[{}] SERVICE: Customer created successfully - ID: {}, Email: {}", 
                    traceId, savedCustomer.getCustomerId(), savedCustomer.getEmail());

            return mapToResponse(savedCustomer);
        } catch (Exception e) {
            log.error("[{}] SERVICE: Failed to create customer with email: {} - Error: {}", 
                    traceId, request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerDto.Response getCustomerById(Long customerId) {
        log.debug("Fetching customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        return mapToResponse(customer);
    }

    /**
     * Get customer by email
     */
    @Transactional(readOnly = true)
    public CustomerDto.Response getCustomerByEmail(String email) {
        log.debug("Fetching customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        return mapToResponse(customer);
    }

    /**
     * Get all customers with pagination
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto.Response> getAllCustomers(Pageable pageable) {
        log.debug("Fetching all customers with pagination: {}", pageable);

        return customerRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Search customers by name or email
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto.Response> searchCustomers(String searchTerm, Pageable pageable) {
        log.debug("Searching customers with term: {}", searchTerm);

        return customerRepository.searchCustomers(searchTerm, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Update customer information
     */
    public CustomerDto.Response updateCustomer(Long customerId, CustomerDto.UpdateRequest request) {
        log.info("Updating customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Check if email is being changed and if it already exists
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Customer with email " + request.getEmail() + " already exists");
            }
            customer.setEmail(request.getEmail());
        }

        if (request.getName() != null) {
            customer.setName(request.getName());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", updatedCustomer.getCustomerId());

        return mapToResponse(updatedCustomer);
    }

    /**
     * Change customer password
     */
    public void changePassword(Long customerId, CustomerDto.ChangePasswordRequest request) {
        log.info("Changing password for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        customer.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);

        log.info("Password changed successfully for customer ID: {}", customerId);
    }

    /**
     * Delete customer
     */
    public void deleteCustomer(Long customerId) {
        log.info("Deleting customer with ID: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }

        customerRepository.deleteById(customerId);
        log.info("Customer deleted successfully with ID: {}", customerId);
    }

    /**
     * Get customers created after a specific date
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto.Response> getCustomersCreatedAfter(LocalDateTime date, Pageable pageable) {
        log.debug("Fetching customers created after: {}", date);

        return customerRepository.findByCreatedAtAfter(date, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get customers with orders
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto.Response> getCustomersWithOrders(Pageable pageable) {
        log.debug("Fetching customers with orders");

        return customerRepository.findCustomersWithOrders(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get customers without orders
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto.Response> getCustomersWithoutOrders(Pageable pageable) {
        log.debug("Fetching customers without orders");

        return customerRepository.findCustomersWithoutOrders(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get total customer count
     */
    @Transactional(readOnly = true)
    public long getTotalCustomerCount() {
        return customerRepository.countTotalCustomers();
    }

    /**
     * Authenticate customer for login
     */
    @Transactional(readOnly = true)
    public Customer authenticateCustomer(String email, String password) {
        log.debug("Authenticating customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        if (!passwordEncoder.matches(password, customer.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return customer;
    }

    /**
     * Check if customer exists by email
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    /**
     * Map Customer entity to Response DTO
     */
    private CustomerDto.Response mapToResponse(Customer customer) {
        return CustomerDto.Response.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .totalOrders(customer.getOrders() != null ? (long) customer.getOrders().size() : 0L)
                .build();
    }
}