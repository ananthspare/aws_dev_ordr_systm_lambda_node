package com.ecommerce.controller;

import com.ecommerce.dto.CustomerDto;
import com.ecommerce.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Create a new customer", description = "Creates a new customer account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Customer with email already exists")
    })
    @PostMapping
    public ResponseEntity<CustomerDto.Response> createCustomer(
            @Valid @RequestBody CustomerDto.CreateRequest request) {
        String traceId = MDC.get("traceId");
        log.info("[{}] Creating new customer with email: {}", traceId, request.getEmail());
        log.debug("[{}] Customer creation request - Name: {}", traceId, request.getName());
        
        try {
            long startTime = System.currentTimeMillis();
            CustomerDto.Response response = customerService.createCustomer(request);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("[{}] Customer created successfully - ID: {}, Email: {}, Duration: {}ms", 
                    traceId, response.getCustomerId(), response.getEmail(), duration);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("[{}] Failed to create customer with email: {} - Error: {}", 
                    traceId, request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a customer by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDto.Response> getCustomerById(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        log.info("Fetching customer with ID: {}", customerId);
        CustomerDto.Response response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get customer by email", description = "Retrieves a customer by their email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDto.Response> getCustomerByEmail(
            @Parameter(description = "Customer email") @PathVariable String email) {
        log.info("Fetching customer with email: {}", email);
        CustomerDto.Response response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all customers", description = "Retrieves all customers with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<CustomerDto.Response>> getAllCustomers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching all customers - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerDto.Response> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search customers", description = "Search customers by name or email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDto.Response>> searchCustomers(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Searching customers with term: {}", searchTerm);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerDto.Response> response = customerService.searchCustomers(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update customer", description = "Updates customer information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDto.Response> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Valid @RequestBody CustomerDto.UpdateRequest request) {
        log.info("Updating customer with ID: {}", customerId);
        CustomerDto.Response response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change customer password", description = "Changes customer password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{customerId}/password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Valid @RequestBody CustomerDto.ChangePasswordRequest request) {
        log.info("Changing password for customer ID: {}", customerId);
        customerService.changePassword(customerId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete customer", description = "Deletes a customer account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        log.info("Deleting customer with ID: {}", customerId);
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get customers created after date", description = "Retrieves customers created after a specific date")
    @GetMapping("/created-after")
    public ResponseEntity<Page<CustomerDto.Response>> getCustomersCreatedAfter(
            @Parameter(description = "Date in ISO format") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching customers created after: {}", date);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CustomerDto.Response> response = customerService.getCustomersCreatedAfter(date, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get customers with orders", description = "Retrieves customers who have placed orders")
    @GetMapping("/with-orders")
    public ResponseEntity<Page<CustomerDto.Response>> getCustomersWithOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching customers with orders");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CustomerDto.Response> response = customerService.getCustomersWithOrders(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get customers without orders", description = "Retrieves customers who haven't placed any orders")
    @GetMapping("/without-orders")
    public ResponseEntity<Page<CustomerDto.Response>> getCustomersWithoutOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching customers without orders");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CustomerDto.Response> response = customerService.getCustomersWithoutOrders(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get total customer count", description = "Returns the total number of customers")
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCustomerCount() {
        log.info("Fetching total customer count");
        long count = customerService.getTotalCustomerCount();
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Check if email exists", description = "Checks if a customer with the given email exists")
    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> checkEmailExists(
            @Parameter(description = "Email to check") @PathVariable String email) {
        log.info("Checking if email exists: {}", email);
        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}