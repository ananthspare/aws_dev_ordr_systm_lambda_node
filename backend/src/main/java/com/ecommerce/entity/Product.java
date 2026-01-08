package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_qty", nullable = false)
    @Builder.Default
    private Integer stockQty = 0;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    // Helper methods
    public boolean isInStock() {
        return stockQty != null && stockQty > 0;
    }

    public boolean hasStock(int quantity) {
        return stockQty != null && stockQty >= quantity;
    }

    public void reduceStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + stockQty + ", Requested: " + quantity);
        }
        this.stockQty -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.stockQty += quantity;
    }
}