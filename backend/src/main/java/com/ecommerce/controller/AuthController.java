package com.ecommerce.controller;

import com.ecommerce.dto.CustomerDto;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final CustomerService customerService;
    private final CartService cartService;

    @Operation(summary = "Register new customer", description = "Creates a new customer account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Customer with email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<CustomerDto.Response> register(
            @Valid @RequestBody CustomerDto.CreateRequest request) {
        log.info("Registering new customer with email: {}", request.getEmail());
        CustomerDto.Response response = customerService.createCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Customer login", description = "Authenticates customer and returns user info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody CustomerDto.LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        try {
            // Authenticate customer
            var customer = customerService.authenticateCustomer(request.getEmail(), request.getPassword());
            
            // Get customer details
            CustomerDto.Response customerResponse = customerService.getCustomerById(customer.getCustomerId());
            
            // Get cart items count
            int cartItemsCount = cartService.getCartItemCount(customer.getCustomerId());
            
            // Return customer info with cart count
            Map<String, Object> response = Map.of(
                "message", "Login successful",
                "customer", customerResponse,
                "token", "mock-jwt-token-" + customer.getCustomerId(),
                "cartItemsCount", cartItemsCount
            );
            
            log.info("Login successful for customer ID: {}", customer.getCustomerId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("Login failed for email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }
    }

    @Operation(summary = "Check email availability", description = "Checks if email is available for registration")
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(
            @RequestParam String email) {
        log.info("Checking email availability: {}", email);
        
        boolean exists = customerService.existsByEmail(email);
        Map<String, Object> response = Map.of(
            "email", email,
            "available", !exists,
            "exists", exists
        );
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Logs out the current user")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("User logout");
        // In a real implementation, you would invalidate the JWT token here
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @Operation(summary = "Refresh token", description = "Refreshes the authentication token")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Token refresh request");
        // In a real implementation, you would validate and refresh the JWT token here
        return ResponseEntity.ok(Map.of(
            "message", "Token refreshed successfully",
            "token", "new-mock-jwt-token-" + System.currentTimeMillis()
        ));
    }
}