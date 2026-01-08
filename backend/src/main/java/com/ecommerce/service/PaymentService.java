package com.ecommerce.service;

import com.ecommerce.dto.PaymentDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * Create a new payment
     */
    public PaymentDto.Response createPayment(PaymentDto.CreateRequest request) {
        log.info("Creating new payment for order ID: {}", request.getOrderId());

        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        // Check if payment already exists for this order
        if (paymentRepository.existsByOrderOrderId(request.getOrderId())) {
            throw new DuplicateResourceException("Payment already exists for order ID: " + request.getOrderId());
        }

        // Create payment
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionRef(request.getTransactionRef())
                .build();

        // For MOCK payment method, automatically mark as PAID
        if (request.getPaymentMethod() == PaymentMethod.MOCK) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setTransactionRef(generateMockTransactionRef());
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully with ID: {}", savedPayment.getPaymentId());

        return mapToResponse(savedPayment);
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentDto.Response getPaymentById(Long paymentId) {
        log.debug("Fetching payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        return mapToResponse(payment);
    }

    /**
     * Get payment by order ID
     */
    @Transactional(readOnly = true)
    public PaymentDto.Response getPaymentByOrderId(Long orderId) {
        log.debug("Fetching payment for order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order ID: " + orderId));

        return mapToResponse(payment);
    }

    /**
     * Get payment by transaction reference
     */
    @Transactional(readOnly = true)
    public PaymentDto.Response getPaymentByTransactionRef(String transactionRef) {
        log.debug("Fetching payment with transaction reference: {}", transactionRef);

        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction reference: " + transactionRef));

        return mapToResponse(payment);
    }

    /**
     * Get all payments with pagination
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getAllPayments(Pageable pageable) {
        log.debug("Fetching all payments with pagination: {}", pageable);

        return paymentRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get payments by status
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        log.debug("Fetching payments with status: {}", status);

        return paymentRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get payments by payment method
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getPaymentsByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable) {
        log.debug("Fetching payments with payment method: {}", paymentMethod);

        return paymentRepository.findByPaymentMethod(paymentMethod, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get payments by customer ID
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getPaymentsByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Fetching payments for customer ID: {}", customerId);

        return paymentRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get payments by date range
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Fetching payments between {} and {}", startDate, endDate);

        return paymentRepository.findByCreatedAtBetween(startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get payments by amount range
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        log.debug("Fetching payments with amount range: {} - {}", minAmount, maxAmount);

        return paymentRepository.findByAmountBetween(minAmount, maxAmount, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get payments by currency
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getPaymentsByCurrency(String currency, Pageable pageable) {
        log.debug("Fetching payments with currency: {}", currency);

        return paymentRepository.findByCurrency(currency, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Update payment status
     */
    public PaymentDto.Response updatePaymentStatus(Long paymentId, PaymentDto.UpdateStatusRequest request) {
        log.info("Updating payment status for ID: {} to {}", paymentId, request.getStatus());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        payment.setStatus(request.getStatus());
        if (request.getTransactionRef() != null) {
            payment.setTransactionRef(request.getTransactionRef());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment status updated successfully for ID: {}", updatedPayment.getPaymentId());

        return mapToResponse(updatedPayment);
    }

    /**
     * Process payment (simulate payment processing)
     */
    public PaymentDto.Response processPayment(Long paymentId) {
        log.info("Processing payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Payment is not in PENDING status. Current status: " + payment.getStatus());
        }

        // Simulate payment processing
        boolean paymentSuccess = simulatePaymentProcessing(payment);

        if (paymentSuccess) {
            payment.markAsPaid(generateTransactionRef(payment.getPaymentMethod()));
            log.info("Payment processed successfully for ID: {}", paymentId);
        } else {
            payment.markAsFailed();
            log.warn("Payment processing failed for ID: {}", paymentId);
        }

        Payment processedPayment = paymentRepository.save(payment);
        return mapToResponse(processedPayment);
    }

    /**
     * Refund payment
     */
    public PaymentDto.Response refundPayment(Long paymentId) {
        log.info("Refunding payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (!payment.getStatus().canRefund()) {
            throw new IllegalArgumentException("Payment cannot be refunded. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment refundedPayment = paymentRepository.save(payment);

        log.info("Payment refunded successfully for ID: {}", refundedPayment.getPaymentId());
        return mapToResponse(refundedPayment);
    }

    /**
     * Get successful payments
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getSuccessfulPayments(Pageable pageable) {
        log.debug("Fetching successful payments");

        return paymentRepository.findSuccessfulPayments(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get failed payments
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getFailedPayments(Pageable pageable) {
        log.debug("Fetching failed payments");

        return paymentRepository.findFailedPayments(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get pending payments
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getPendingPayments(Pageable pageable) {
        log.debug("Fetching pending payments");

        return paymentRepository.findPendingPayments(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get payments requiring retry
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.Response> getPaymentsRequiringRetry() {
        log.debug("Fetching payments requiring retry");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        return paymentRepository.findPaymentsRequiringRetry(cutoffTime)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get recent payments for customer
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.Response> getRecentPaymentsByCustomer(Long customerId, int limit) {
        log.debug("Fetching recent payments for customer ID: {}", customerId);

        Pageable pageable = Pageable.ofSize(limit);
        return paymentRepository.findRecentPaymentsByCustomer(customerId, pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get payment statistics
     */
    @Transactional(readOnly = true)
    public PaymentDto.Statistics getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Object stats = paymentRepository.getPaymentStatistics(startDate, endDate).orElse(null);
        
        // Calculate basic statistics
        long totalPayments = paymentRepository.count();
        BigDecimal totalAmount = paymentRepository.getTotalAmountByStatus(PaymentStatus.PAID);
        long successfulPayments = paymentRepository.countByStatus(PaymentStatus.PAID);
        long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);
        
        Double successRate = totalPayments > 0 ? 
                (double) successfulPayments / totalPayments * 100 : 0.0;

        return PaymentDto.Statistics.builder()
                .totalPayments(totalPayments)
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .averageAmount(totalPayments > 0 && totalAmount != null ? 
                        totalAmount.divide(BigDecimal.valueOf(totalPayments), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .successfulPayments(successfulPayments)
                .failedPayments(failedPayments)
                .pendingPayments(pendingPayments)
                .successRate(successRate)
                .build();
    }

    /**
     * Calculate total amount by status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByStatus(PaymentStatus status) {
        return paymentRepository.getTotalAmountByStatus(status);
    }

    /**
     * Calculate total amount by payment method
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByPaymentMethod(PaymentMethod paymentMethod) {
        return paymentRepository.getTotalAmountByPaymentMethod(paymentMethod);
    }

    /**
     * Calculate success rate
     */
    @Transactional(readOnly = true)
    public Double calculateSuccessRate(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.calculateSuccessRate(startDate, endDate);
    }

    /**
     * Simulate payment processing (for demo purposes)
     */
    private boolean simulatePaymentProcessing(Payment payment) {
        // For MOCK payments, always succeed
        if (payment.getPaymentMethod() == PaymentMethod.MOCK) {
            return true;
        }
        
        // For other payment methods, simulate 95% success rate
        return Math.random() < 0.95;
    }

    /**
     * Generate transaction reference
     */
    private String generateTransactionRef(PaymentMethod paymentMethod) {
        String prefix = switch (paymentMethod) {
            case MOCK -> "MOCK";
            case CARD -> "CARD";
            case UPI -> "UPI";
            case PAYPAL -> "PP";
            case BANK_TRANSFER -> "BT";
            case CASH_ON_DELIVERY -> "COD";
        };
        
        return prefix + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generate mock transaction reference
     */
    private String generateMockTransactionRef() {
        return "MOCK_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Map Payment entity to Response DTO
     */
    private PaymentDto.Response mapToResponse(Payment payment) {
        return PaymentDto.Response.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionRef(payment.getTransactionRef())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .customerName(payment.getOrder().getCustomer().getName())
                .customerEmail(payment.getOrder().getCustomer().getEmail())
                .build();
    }
}