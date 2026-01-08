package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemDto {

    private Long orderItemId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    // Additional fields for response
    private BigDecimal totalPrice;
    private ProductDto.Response product;

    // Response DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private Long orderItemId;
        private Long orderId;
        private Long productId;
        private String productName;
        private String productDescription;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private ProductDto.Response product;
    }

    // Create request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

    // Update request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
        private BigDecimal unitPrice;
    }

    // Order item summary DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}