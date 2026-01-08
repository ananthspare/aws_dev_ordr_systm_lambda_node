package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by name containing (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find products by price range
     */
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find products with stock greater than specified quantity
     */
    Page<Product> findByStockQtyGreaterThan(Integer quantity, Pageable pageable);

    /**
     * Find products that are in stock
     */
    @Query("SELECT p FROM Product p WHERE p.stockQty > 0")
    Page<Product> findInStockProducts(Pageable pageable);

    /**
     * Find products that are out of stock
     */
    @Query("SELECT p FROM Product p WHERE p.stockQty = 0")
    Page<Product> findOutOfStockProducts(Pageable pageable);

    /**
     * Find products with low stock (less than specified threshold)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQty > 0 AND p.stockQty <= :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products created after a specific date
     */
    Page<Product> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    /**
     * Update product stock quantity
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQty = p.stockQty - :quantity WHERE p.productId = :productId AND p.stockQty >= :quantity")
    int reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Increase product stock quantity
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQty = p.stockQty + :quantity WHERE p.productId = :productId")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Count products by stock status
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQty > 0")
    long countInStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQty = 0")
    long countOutOfStockProducts();

    /**
     * Find top selling products (based on order items)
     */
    @Query("SELECT p FROM Product p JOIN p.orderItems oi " +
           "GROUP BY p ORDER BY SUM(oi.quantity) DESC")
    Page<Product> findTopSellingProducts(Pageable pageable);

    /**
     * Find products by price range and stock availability
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.stockQty > 0")
    Page<Product> findAvailableProductsByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                                   @Param("maxPrice") BigDecimal maxPrice, 
                                                   Pageable pageable);
}