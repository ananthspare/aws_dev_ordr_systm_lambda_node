package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.enums.OrderStatus;
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
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by customer ID
     */
    Page<Order> findByCustomerCustomerId(Long customerId, Pageable pageable);

    /**
     * Find orders by status
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Find orders by payment status
     */
    Page<Order> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Find orders by country code
     */
    Page<Order> findByCountryCode(String countryCode, Pageable pageable);

    /**
     * Find orders created between dates
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders by customer and status
     */
    Page<Order> findByCustomerCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    /**
     * Find orders by customer and date range
     */
    Page<Order> findByCustomerCustomerIdAndCreatedAtBetween(Long customerId, 
                                                           LocalDateTime startDate, 
                                                           LocalDateTime endDate, 
                                                           Pageable pageable);

    /**
     * Find orders with total amount greater than specified value
     */
    Page<Order> findByTotalAmountGreaterThan(BigDecimal amount, Pageable pageable);

    /**
     * Find orders with invoice S3 path
     */
    @Query("SELECT o FROM Order o WHERE o.invoiceS3Path IS NOT NULL")
    Page<Order> findOrdersWithInvoice(Pageable pageable);

    /**
     * Find orders without invoice S3 path
     */
    @Query("SELECT o FROM Order o WHERE o.invoiceS3Path IS NULL")
    Page<Order> findOrdersWithoutInvoice(Pageable pageable);

    /**
     * Count orders by status
     */
    long countByStatus(OrderStatus status);

    /**
     * Count orders by payment status
     */
    long countByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Count orders by customer
     */
    long countByCustomerCustomerId(Long customerId);

    /**
     * Find recent orders for a customer
     */
    @Query("SELECT o FROM Order o WHERE o.customer.customerId = :customerId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByCustomer(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Calculate total revenue
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
    BigDecimal calculateTotalRevenue();

    /**
     * Calculate revenue by date range
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate revenue by country
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.countryCode = :countryCode")
    BigDecimal calculateRevenueByCountry(@Param("countryCode") String countryCode);

    /**
     * Find orders by multiple statuses
     */
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    Page<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

    /**
     * Find orders requiring invoice generation
     */
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'PAID' AND o.invoiceS3Path IS NULL")
    List<Order> findOrdersRequiringInvoice();

    /**
     * Search orders by customer name or email
     */
    @Query("SELECT o FROM Order o JOIN o.customer c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Order> searchOrdersByCustomer(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find orders by status and country
     */
    Page<Order> findByStatusAndCountryCode(OrderStatus status, String countryCode, Pageable pageable);

    /**
     * Get order statistics by date range
     */
    @Query("SELECT new map(" +
           "COUNT(o) as totalOrders, " +
           "COALESCE(SUM(o.totalAmount), 0) as totalRevenue, " +
           "COALESCE(AVG(o.totalAmount), 0) as averageOrderValue) " +
           "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Optional<Object> getOrderStatistics(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
}