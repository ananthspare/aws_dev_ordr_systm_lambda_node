package com.ecommerce.controller;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.service.OrderService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Creates a new order with order items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Customer or product not found")
    })
    @PostMapping
    public ResponseEntity<OrderDto.Response> createOrder(
            @Valid @RequestBody OrderDto.CreateRequest request) {
        String traceId = MDC.get("traceId");
        log.info("[{}] Creating new order for customer ID: {} with {} items", 
                traceId, request.getCustomerId(), request.getOrderItems().size());
        log.debug("[{}] Order creation request details: {}", traceId, request);
        
        try {
            long startTime = System.currentTimeMillis();
            OrderDto.Response response = orderService.createOrder(request);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("[{}] Order created successfully - ID: {}, Total: ${}, Duration: {}ms", 
                    traceId, response.getOrderId(), response.getTotalAmount(), duration);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("[{}] Failed to create order for customer ID: {} - Error: {}", 
                    traceId, request.getCustomerId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Checkout order", description = "Creates order and processes payment in single transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order placed and payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Customer or product not found")
    })
    @PostMapping("/checkout")
    public ResponseEntity<OrderDto.CheckoutResponse> checkout(@Valid @RequestBody OrderDto.CheckoutRequest request) {
        log.info("Processing checkout for customer ID: {}", request.getCustomerId());
        OrderDto.CheckoutResponse response = orderService.checkout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto.Response> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        String traceId = MDC.get("traceId");
        log.info("[{}] Fetching order with ID: {}", traceId, orderId);
        
        try {
            long startTime = System.currentTimeMillis();
            OrderDto.Response response = orderService.getOrderById(orderId);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("[{}] Order retrieved successfully - ID: {}, Status: {}, Duration: {}ms", 
                    traceId, response.getOrderId(), response.getStatus(), duration);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[{}] Failed to fetch order ID: {} - Error: {}", traceId, orderId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Get all orders", description = "Retrieves all orders with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<OrderDto.Response>> getAllOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        String traceId = MDC.get("traceId");
        log.info("[{}] Fetching all orders - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                traceId, page, size, sortBy, sortDir);
        
        try {
            long startTime = System.currentTimeMillis();
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDto.Response> response = orderService.getAllOrders(pageable);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("[{}] Orders retrieved successfully - Total: {}, Page: {}/{}, Duration: {}ms", 
                    traceId, response.getTotalElements(), response.getNumber() + 1, 
                    response.getTotalPages(), duration);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[{}] Failed to fetch orders - Error: {}", traceId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Get orders by customer", description = "Retrieves orders for a specific customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderDto.Response>> getOrdersByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching orders for customer ID: {}", customerId);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderDto.Response> response = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get my orders", description = "Retrieves orders for the currently authenticated customer")
    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderDto.Response>> getMyOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        // Get customer ID from security context
        String customerIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long customerId = Long.parseLong(customerIdStr);
        
        log.info("Fetching orders for authenticated customer ID: {}", customerId);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderDto.Response> response = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get orders by status", description = "Retrieves orders with a specific status")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderDto.Response>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable OrderStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching orders with status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDto.Response> response = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get orders by payment status", description = "Retrieves orders with a specific payment status")
    @GetMapping("/payment-status/{paymentStatus}")
    public ResponseEntity<Page<OrderDto.Response>> getOrdersByPaymentStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus paymentStatus,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching orders with payment status: {}", paymentStatus);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDto.Response> response = orderService.getOrdersByPaymentStatus(paymentStatus, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get orders by country", description = "Retrieves orders for a specific country")
    @GetMapping("/country/{countryCode}")
    public ResponseEntity<Page<OrderDto.Response>> getOrdersByCountryCode(
            @Parameter(description = "Country code (2 letters)") @PathVariable String countryCode,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching orders for country: {}", countryCode);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDto.Response> response = orderService.getOrdersByCountryCode(countryCode, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get orders by date range", description = "Retrieves orders within a date range")
    @GetMapping("/date-range")
    public ResponseEntity<Page<OrderDto.Response>> getOrdersByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching orders between {} and {}", startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDto.Response> response = orderService.getOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update order status", description = "Updates the status of an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto.Response> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody OrderDto.UpdateStatusRequest request) {
        log.info("Updating order status for ID: {} to {}", orderId, request.getStatus());
        OrderDto.Response response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update payment status", description = "Updates the payment status of an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderDto.Response> updatePaymentStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody OrderDto.UpdatePaymentStatusRequest request) {
        log.info("Updating payment status for order ID: {} to {}", orderId, request.getPaymentStatus());
        OrderDto.Response response = orderService.updatePaymentStatus(orderId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update invoice S3 path", description = "Updates the S3 path for order invoice")
    @PutMapping("/{orderId}/invoice")
    public ResponseEntity<OrderDto.Response> updateInvoiceS3Path(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "S3 path") @RequestParam String s3Path) {
        log.info("Updating invoice S3 path for order ID: {}", orderId);
        OrderDto.Response response = orderService.updateInvoiceS3Path(orderId, s3Path);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel order", description = "Cancels an order and restores stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto.Response> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        OrderDto.Response response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search orders by customer", description = "Search orders by customer name or email")
    @GetMapping("/search")
    public ResponseEntity<Page<OrderDto.Response>> searchOrdersByCustomer(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching orders by customer with term: {}", searchTerm);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDto.Response> response = orderService.searchOrdersByCustomer(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get orders with invoices", description = "Retrieves orders that have invoice files")
    @GetMapping("/with-invoice")
    public ResponseEntity<Page<OrderDto.Response>> getOrdersWithInvoice(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching orders with invoices");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDto.Response> response = orderService.getOrdersWithInvoice(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get orders without invoices", description = "Retrieves orders that need invoice generation")
    @GetMapping("/without-invoice")
    public ResponseEntity<Page<OrderDto.Response>> getOrdersWithoutInvoice(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching orders without invoices");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDto.Response> response = orderService.getOrdersWithoutInvoice(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get recent orders for customer", description = "Retrieves recent orders for a customer")
    @GetMapping("/customer/{customerId}/recent")
    public ResponseEntity<List<OrderDto.Response>> getRecentOrdersByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "5") int limit) {
        
        log.info("Fetching recent orders for customer ID: {}", customerId);
        
        List<OrderDto.Response> response = orderService.getRecentOrdersByCustomer(customerId, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get total revenue", description = "Returns total revenue from all paid orders")
    @GetMapping("/revenue/total")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        log.info("Fetching total revenue");
        BigDecimal revenue = orderService.calculateTotalRevenue();
        return ResponseEntity.ok(revenue);
    }

    @Operation(summary = "Get revenue by date range", description = "Returns revenue within a date range")
    @GetMapping("/revenue/date-range")
    public ResponseEntity<BigDecimal> getRevenueByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching revenue between {} and {}", startDate, endDate);
        BigDecimal revenue = orderService.calculateRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @Operation(summary = "Get revenue by country", description = "Returns revenue for a specific country")
    @GetMapping("/revenue/country/{countryCode}")
    public ResponseEntity<BigDecimal> getRevenueByCountry(
            @Parameter(description = "Country code") @PathVariable String countryCode) {
        
        log.info("Fetching revenue for country: {}", countryCode);
        BigDecimal revenue = orderService.calculateRevenueByCountry(countryCode);
        return ResponseEntity.ok(revenue);
    }

    @Operation(summary = "Get order statistics", description = "Returns order statistics for a date range")
    @GetMapping("/statistics")
    public ResponseEntity<OrderDto.Statistics> getOrderStatistics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching order statistics between {} and {}", startDate, endDate);
        OrderDto.Statistics statistics = orderService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
}