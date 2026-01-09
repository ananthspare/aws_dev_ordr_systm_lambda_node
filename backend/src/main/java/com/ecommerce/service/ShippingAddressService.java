package com.ecommerce.service;

import com.ecommerce.dto.ShippingAddressDto;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.ShippingAddress;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.ShippingAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final CustomerRepository customerRepository;

    public ShippingAddressDto.Response createShippingAddress(ShippingAddressDto.CreateRequest request) {
        log.info("Creating shipping address for customer ID: {}", request.getCustomerId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        if (request.getIsDefault() != null && request.getIsDefault()) {
            shippingAddressRepository.clearDefaultAddressForCustomer(request.getCustomerId());
        }

        ShippingAddress shippingAddress = ShippingAddress.builder()
                .customer(customer)
                .fullName(request.getFullName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .countryCode(request.getCountryCode())
                .phoneNumber(request.getPhoneNumber())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        ShippingAddress savedAddress = shippingAddressRepository.save(shippingAddress);
        return mapToResponse(savedAddress);
    }

    @Transactional(readOnly = true)
    public List<ShippingAddressDto.Response> getShippingAddressesByCustomer(Long customerId) {
        log.info("Fetching shipping addresses for customer ID: {}", customerId);
        
        return shippingAddressRepository.findByCustomerCustomerIdOrderByIsDefaultDescCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShippingAddressDto.Response getShippingAddressById(Long addressId) {
        log.info("Fetching shipping address with ID: {}", addressId);
        
        ShippingAddress address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with ID: " + addressId));
        
        return mapToResponse(address);
    }

    @Transactional(readOnly = true)
    public ShippingAddress getShippingAddressEntityById(Long addressId) {
        log.info("Fetching shipping address entity with ID: {}", addressId);
        
        return shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with ID: " + addressId));
    }

    private ShippingAddressDto.Response mapToResponse(ShippingAddress address) {
        return ShippingAddressDto.Response.builder()
                .shippingAddressId(address.getShippingAddressId())
                .customerId(address.getCustomer().getCustomerId())
                .fullName(address.getFullName())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .countryCode(address.getCountryCode())
                .phoneNumber(address.getPhoneNumber())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}