package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find order items by order ID
     */
    List<OrderItem> findByOrderOrderId(Long orderId);

    /**
     * Find order items by product ID
     */
    Page<OrderItem> findByProductProductId(Long productId, Pageable pageable);

    /**
     * Find order items by order ID with pagination
     */
    Page<OrderItem> findByOrderOrderId(Long orderId, Pageable pageable);

    /**
     * Calculate total quantity sold for a product
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.product.productId = :productId")
    Long getTotalQuantitySoldForProduct(@Param("productId") Long productId);

    /**
     * Calculate total revenue for a product
     */
    @Query("SELECT COALESCE(SUM(oi.quantity * oi.unitPrice), 0) FROM OrderItem oi WHERE oi.product.productId = :productId")
    BigDecimal getTotalRevenueForProduct(@Param("productId") Long productId);

    /**
     * Find top selling products by quantity
     */
    @Query("SELECT oi.product.productId, oi.product.name, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi " +
           "GROUP BY oi.product.productId, oi.product.name " +
           "ORDER BY totalSold DESC")
    Page<Object[]> findTopSellingProductsByQuantity(Pageable pageable);

    /**
     * Find top selling products by revenue
     */
    @Query("SELECT oi.product.productId, oi.product.name, SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi " +
           "GROUP BY oi.product.productId, oi.product.name " +
           "ORDER BY totalRevenue DESC")
    Page<Object[]> findTopSellingProductsByRevenue(Pageable pageable);

    /**
     * Count order items by order
     */
    long countByOrderOrderId(Long orderId);

    /**
     * Find order items with quantity greater than specified value
     */
    Page<OrderItem> findByQuantityGreaterThan(Integer quantity, Pageable pageable);

    /**
     * Find order items with unit price in range
     */
    Page<OrderItem> findByUnitPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Calculate total value of order items for an order
     */
    @Query("SELECT COALESCE(SUM(oi.quantity * oi.unitPrice), 0) FROM OrderItem oi WHERE oi.order.orderId = :orderId")
    BigDecimal calculateOrderTotal(@Param("orderId") Long orderId);

    /**
     * Find order items by customer (through order relationship)
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.customer.customerId = :customerId")
    Page<OrderItem> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Find order items by product name (case-insensitive)
     */
    @Query("SELECT oi FROM OrderItem oi WHERE LOWER(oi.product.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    Page<OrderItem> findByProductNameContaining(@Param("productName") String productName, Pageable pageable);

    /**
     * Get product sales statistics
     */
    @Query("SELECT new map(" +
           "oi.product.productId as productId, " +
           "oi.product.name as productName, " +
           "COUNT(oi) as orderCount, " +
           "SUM(oi.quantity) as totalQuantity, " +
           "SUM(oi.quantity * oi.unitPrice) as totalRevenue, " +
           "AVG(oi.unitPrice) as averagePrice) " +
           "FROM OrderItem oi " +
           "WHERE oi.product.productId = :productId " +
           "GROUP BY oi.product.productId, oi.product.name")
    Object getProductSalesStatistics(@Param("productId") Long productId);

    /**
     * Find order items by order status (through order relationship)
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.status = :status")
    Page<OrderItem> findByOrderStatus(@Param("status") com.ecommerce.enums.OrderStatus status, Pageable pageable);

    /**
     * Delete order items by order ID
     */
    void deleteByOrderOrderId(Long orderId);
}