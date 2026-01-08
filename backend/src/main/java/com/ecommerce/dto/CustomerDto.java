package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDto {

    private Long customerId;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required", groups = CreateValidation.class)
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private LocalDateTime createdAt;

    // Validation groups
    public interface CreateValidation {}
    public interface UpdateValidation {}

    // Response DTO without password
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private Long customerId;
        private String name;
        private String email;
        private LocalDateTime createdAt;
        private Long totalOrders;
        private String lastOrderDate;
    }

    // Create request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        private String password;
    }

    // Update request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 150, message = "Name must not exceed 150 characters")
        private String name;

        @Email(message = "Email should be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        private String email;
    }

    // Login request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    // Change password request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
        private String newPassword;
    }
}