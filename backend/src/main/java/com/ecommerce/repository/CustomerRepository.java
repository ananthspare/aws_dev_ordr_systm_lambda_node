package com.ecommerce.repository;

import com.ecommerce.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by email address
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Check if customer exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find customers by name containing (case-insensitive)
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find customers created after a specific date
     */
    Page<Customer> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    /**
     * Find customers created between two dates
     */
    Page<Customer> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Count total customers
     */
    @Query("SELECT COUNT(c) FROM Customer c")
    long countTotalCustomers();

    /**
     * Find customers with orders
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.orders o")
    Page<Customer> findCustomersWithOrders(Pageable pageable);

    /**
     * Find customers without orders
     */
    @Query("SELECT c FROM Customer c WHERE c.customerId NOT IN (SELECT DISTINCT o.customer.customerId FROM Order o)")
    Page<Customer> findCustomersWithoutOrders(Pageable pageable);

    /**
     * Search customers by email or name
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);
}