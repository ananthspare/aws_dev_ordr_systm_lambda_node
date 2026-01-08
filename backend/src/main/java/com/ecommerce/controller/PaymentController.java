package com.ecommerce.controller;

import com.ecommerce.dto.PaymentDto;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a new payment", description = "Creates a new payment for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Payment already exists for order")
    })
    @PostMapping
    public ResponseEntity<PaymentDto.Response> createPayment(
            @Valid @RequestBody PaymentDto.CreateRequest request) {
        log.info("Creating new payment for order ID: {}", request.getOrderId());
        PaymentDto.Response response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto.Response> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        PaymentDto.Response response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment by order ID", description = "Retrieves payment for a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found for order")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto.Response> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        PaymentDto.Response response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment by transaction reference", description = "Retrieves payment by transaction reference")
    @GetMapping("/transaction/{transactionRef}")
    public ResponseEntity<PaymentDto.Response> getPaymentByTransactionRef(
            @Parameter(description = "Transaction reference") @PathVariable String transactionRef) {
        log.info("Fetching payment with transaction reference: {}", transactionRef);
        PaymentDto.Response response = paymentService.getPaymentByTransactionRef(transactionRef);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all payments", description = "Retrieves all payments with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<PaymentDto.Response>> getAllPayments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching all payments - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PaymentDto.Response> response = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payments by status", description = "Retrieves payments with a specific status")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PaymentDto.Response>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching payments with status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentDto.Response> response = paymentService.getPaymentsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payments by payment method", description = "Retrieves payments with a specific payment method")
    @GetMapping("/method/{paymentMethod}")
    public ResponseEntity<Page<PaymentDto.Response>> getPaymentsByPaymentMethod(
            @Parameter(description = "Payment method") @PathVariable PaymentMethod paymentMethod,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching payments with payment method: {}", paymentMethod);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentDto.Response> response = paymentService.getPaymentsByPaymentMethod(paymentMethod, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payments by customer", description = "Retrieves payments for a specific customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<PaymentDto.Response>> getPaymentsByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching payments for customer ID: {}", customerId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentDto.Response> response = paymentService.getPaymentsByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payments by date range", description = "Retrieves payments within a date range")
    @GetMapping("/date-range")
    public ResponseEntity<Page<PaymentDto.Response>> getPaymentsByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching payments between {} and {}", startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentDto.Response> response = paymentService.getPaymentsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payments by amount range", description = "Retrieves payments within an amount range")
    @GetMapping("/amount-range")
    public ResponseEntity<Page<PaymentDto.Response>> getPaymentsByAmountRange(
            @Parameter(description = "Minimum amount") @RequestParam BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam BigDecimal maxAmount,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching payments with amount range: {} - {}", minAmount, maxAmount);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("amount").ascending());
        Page<PaymentDto.Response> response = paymentService.getPaymentsByAmountRange(minAmount, maxAmount, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payments by currency", description = "Retrieves payments with a specific currency")
    @GetMapping("/currency/{currency}")
    public ResponseEntity<Page<PaymentDto.Response>> getPaymentsByCurrency(
            @Parameter(description = "Currency code") @PathVariable String currency,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching payments with currency: {}", currency);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentDto.Response> response = paymentService.getPaymentsByCurrency(currency, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update payment status", description = "Updates the status of a payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentDto.Response> updatePaymentStatus(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId,
            @Valid @RequestBody PaymentDto.UpdateStatusRequest request) {
        log.info("Updating payment status for ID: {} to {}", paymentId, request.getStatus());
        PaymentDto.Response response = paymentService.updatePaymentStatus(paymentId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Process payment", description = "Processes a pending payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Payment not in pending status"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/{paymentId}/process")
    public ResponseEntity<PaymentDto.Response> processPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        log.info("Processing payment with ID: {}", paymentId);
        PaymentDto.Response response = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refund payment", description = "Refunds a successful payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment refunded successfully"),
            @ApiResponse(responseCode = "400", description = "Payment cannot be refunded"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentDto.Response> refundPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        log.info("Refunding payment with ID: {}", paymentId);
        PaymentDto.Response response = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get successful payments", description = "Retrieves all successful payments")
    @GetMapping("/successful")
    public ResponseEntity<Page<PaymentDto.Response>> getSuccessfulPayments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching successful payments");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentDto.Response> response = paymentService.getSuccessfulPayments(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get failed payments", description = "Retrieves all failed payments")
    @GetMapping("/failed")
    public ResponseEntity<Page<PaymentDto.Response>> getFailedPayments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching failed payments");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentDto.Response> response = paymentService.getFailedPayments(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get pending payments", description = "Retrieves all pending payments")
    @GetMapping("/pending")
    public ResponseEntity<Page<PaymentDto.Response>> getPendingPayments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching pending payments");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<PaymentDto.Response> response = paymentService.getPendingPayments(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payments requiring retry", description = "Retrieves failed payments that can be retried")
    @GetMapping("/retry")
    public ResponseEntity<List<PaymentDto.Response>> getPaymentsRequiringRetry() {
        log.info("Fetching payments requiring retry");
        List<PaymentDto.Response> response = paymentService.getPaymentsRequiringRetry();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get recent payments for customer", description = "Retrieves recent payments for a customer")
    @GetMapping("/customer/{customerId}/recent")
    public ResponseEntity<List<PaymentDto.Response>> getRecentPaymentsByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "5") int limit) {
        
        log.info("Fetching recent payments for customer ID: {}", customerId);
        
        List<PaymentDto.Response> response = paymentService.getRecentPaymentsByCustomer(customerId, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment statistics", description = "Returns payment statistics for a date range")
    @GetMapping("/statistics")
    public ResponseEntity<PaymentDto.Statistics> getPaymentStatistics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching payment statistics between {} and {}", startDate, endDate);
        PaymentDto.Statistics statistics = paymentService.getPaymentStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get total amount by status", description = "Returns total payment amount for a specific status")
    @GetMapping("/total/status/{status}")
    public ResponseEntity<BigDecimal> getTotalAmountByStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus status) {
        
        log.info("Fetching total amount for status: {}", status);
        BigDecimal totalAmount = paymentService.getTotalAmountByStatus(status);
        return ResponseEntity.ok(totalAmount);
    }

    @Operation(summary = "Get total amount by payment method", description = "Returns total payment amount for a specific payment method")
    @GetMapping("/total/method/{paymentMethod}")
    public ResponseEntity<BigDecimal> getTotalAmountByPaymentMethod(
            @Parameter(description = "Payment method") @PathVariable PaymentMethod paymentMethod) {
        
        log.info("Fetching total amount for payment method: {}", paymentMethod);
        BigDecimal totalAmount = paymentService.getTotalAmountByPaymentMethod(paymentMethod);
        return ResponseEntity.ok(totalAmount);
    }

    @Operation(summary = "Get success rate", description = "Returns payment success rate for a date range")
    @GetMapping("/success-rate")
    public ResponseEntity<Double> getSuccessRate(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching success rate between {} and {}", startDate, endDate);
        Double successRate = paymentService.calculateSuccessRate(startDate, endDate);
        return ResponseEntity.ok(successRate);
    }
}