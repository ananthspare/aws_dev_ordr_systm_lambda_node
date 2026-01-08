package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long customerId;
    private List<CartItemDto> items;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private String lastUpdated;
}