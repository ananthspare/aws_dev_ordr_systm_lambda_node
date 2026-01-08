package com.ecommerce.repository;

import com.ecommerce.entity.Payment;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by order ID
     */
    Optional<Payment> findByOrderOrderId(Long orderId);

    /**
     * Find payments by status
     */
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    /**
     * Find payments by payment method
     */
    Page<Payment> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * Find payments by transaction reference
     */
    Optional<Payment> findByTransactionRef(String transactionRef);

    /**
     * Find payments created between dates
     */
    Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find payments by amount range
     */
    Page<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Find payments by currency
     */
    Page<Payment> findByCurrency(String currency, Pageable pageable);

    /**
     * Find payments by customer (through order relationship)
     */
    @Query("SELECT p FROM Payment p WHERE p.order.customer.customerId = :customerId")
    Page<Payment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);

    /**
     * Count payments by payment method
     */
    long countByPaymentMethod(PaymentMethod paymentMethod);

    /**
     * Calculate total payment amount by status
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") PaymentStatus status);

    /**
     * Calculate total payment amount by payment method
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentMethod = :paymentMethod")
    BigDecimal getTotalAmountByPaymentMethod(@Param("paymentMethod") PaymentMethod paymentMethod);

    /**
     * Calculate total payment amount by date range
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find successful payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PAID'")
    Page<Payment> findSuccessfulPayments(Pageable pageable);

    /**
     * Find failed payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED'")
    Page<Payment> findFailedPayments(Pageable pageable);

    /**
     * Find pending payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING'")
    Page<Payment> findPendingPayments(Pageable pageable);

    /**
     * Find payments requiring retry (failed payments within last 24 hours)
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt > :cutoffTime")
    List<Payment> findPaymentsRequiringRetry(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get payment statistics by date range
     */
    @Query("SELECT new map(" +
           "COUNT(p) as totalPayments, " +
           "COALESCE(SUM(p.amount), 0) as totalAmount, " +
           "COALESCE(AVG(p.amount), 0) as averageAmount, " +
           "COUNT(CASE WHEN p.status = 'PAID' THEN 1 END) as successfulPayments, " +
           "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedPayments) " +
           "FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Optional<Object> getPaymentStatistics(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Find payments by multiple statuses
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN :statuses")
    Page<Payment> findByStatusIn(@Param("statuses") List<PaymentStatus> statuses, Pageable pageable);

    /**
     * Find payments by order country code (through order relationship)
     */
    @Query("SELECT p FROM Payment p WHERE p.order.countryCode = :countryCode")
    Page<Payment> findByOrderCountryCode(@Param("countryCode") String countryCode, Pageable pageable);

    /**
     * Find recent payments for a customer
     */
    @Query("SELECT p FROM Payment p WHERE p.order.customer.customerId = :customerId ORDER BY p.createdAt DESC")
    List<Payment> findRecentPaymentsByCustomer(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Check if payment exists for order
     */
    boolean existsByOrderOrderId(Long orderId);

    /**
     * Find payments updated after specific time
     */
    Page<Payment> findByUpdatedAtAfter(LocalDateTime updatedAfter, Pageable pageable);

    /**
     * Calculate payment success rate
     */
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN p.status = 'PAID' THEN 1 END) AS double) / CAST(COUNT(p) AS double) * 100 " +
           "FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Double calculateSuccessRate(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
}