package com.ecommerce.controller;

import com.ecommerce.dto.ShippingAddressDto;
import com.ecommerce.service.ShippingAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping-addresses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shipping Address Management", description = "APIs for managing shipping addresses")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @Operation(summary = "Create shipping address", description = "Creates a new shipping address for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shipping address created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping
    public ResponseEntity<ShippingAddressDto.Response> createShippingAddress(
            @Valid @RequestBody ShippingAddressDto.CreateRequest request) {
        log.info("Creating shipping address for customer ID: {}", request.getCustomerId());
        ShippingAddressDto.Response response = shippingAddressService.createShippingAddress(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get my shipping addresses", description = "Retrieves shipping addresses for the authenticated customer")
    @GetMapping("/my-addresses")
    public ResponseEntity<List<ShippingAddressDto.Response>> getMyShippingAddresses() {
        String customerIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long customerId = Long.parseLong(customerIdStr);
        
        log.info("Fetching shipping addresses for authenticated customer ID: {}", customerId);
        List<ShippingAddressDto.Response> addresses = shippingAddressService.getShippingAddressesByCustomer(customerId);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Get shipping addresses by customer", description = "Retrieves shipping addresses for a specific customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ShippingAddressDto.Response>> getShippingAddressesByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        log.info("Fetching shipping addresses for customer ID: {}", customerId);
        List<ShippingAddressDto.Response> addresses = shippingAddressService.getShippingAddressesByCustomer(customerId);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Get shipping address by ID", description = "Retrieves a shipping address by its ID")
    @GetMapping("/{addressId}")
    public ResponseEntity<ShippingAddressDto.Response> getShippingAddressById(
            @Parameter(description = "Shipping address ID") @PathVariable Long addressId) {
        log.info("Fetching shipping address with ID: {}", addressId);
        ShippingAddressDto.Response address = shippingAddressService.getShippingAddressById(addressId);
        return ResponseEntity.ok(address);
    }
}