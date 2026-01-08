package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {

    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQty;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    private LocalDateTime createdAt;

    // Additional fields for response
    private Boolean inStock;
    private Long totalSold;
    private BigDecimal totalRevenue;

    // Response DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private Long productId;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQty;
        private String imageUrl;
        private LocalDateTime createdAt;
        private Boolean inStock;
        private Long totalSold;
        private BigDecimal totalRevenue;
    }

    // Create request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        private BigDecimal price;

        @Min(value = 0, message = "Stock quantity cannot be negative")
        private Integer stockQty = 0;

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String imageUrl;
    }

    // Update request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        private BigDecimal price;

        @Min(value = 0, message = "Stock quantity cannot be negative")
        private Integer stockQty;

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String imageUrl;
    }

    // Stock update request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockUpdateRequest {
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        private String operation; // "increase" or "decrease"
    }

    // Product search request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String name;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Boolean inStockOnly;
        private String sortBy; // "name", "price", "createdAt", "stockQty"
        private String sortDirection; // "asc", "desc"
    }

    // Product statistics DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long productId;
        private String productName;
        private Long totalQuantity;
        private Long orderCount;
        private BigDecimal totalRevenue;
        private BigDecimal averagePrice;
    }
}