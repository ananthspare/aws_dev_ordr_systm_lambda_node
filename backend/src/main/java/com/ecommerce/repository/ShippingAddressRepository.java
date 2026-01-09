package com.ecommerce.repository;

import com.ecommerce.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    List<ShippingAddress> findByCustomerCustomerIdOrderByIsDefaultDescCreatedAtDesc(Long customerId);

    Optional<ShippingAddress> findByCustomerCustomerIdAndIsDefaultTrue(Long customerId);

    @Query("UPDATE ShippingAddress sa SET sa.isDefault = false WHERE sa.customer.customerId = :customerId")
    void clearDefaultAddressForCustomer(@Param("customerId") Long customerId);
}