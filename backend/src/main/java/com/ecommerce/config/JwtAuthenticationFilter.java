package com.ecommerce.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                String customerId;
                
                // Handle mock tokens for testing
                if (token.startsWith("mock-jwt-token-")) {
                    customerId = token.replace("mock-jwt-token-", "");
                } else {
                    // For real JWT tokens, extract customer ID from payload
                    // This is a simplified version - in production, validate signature
                    String[] parts = token.split("\\.");
                    if (parts.length == 3) {
                        // Decode payload (base64)
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                        // Extract sub claim (customer ID) - simplified JSON parsing
                        customerId = payload.replaceAll(".*\"sub\":\"([^\"]+)\".*", "$1");
                    } else {
                        throw new IllegalArgumentException("Invalid JWT format");
                    }
                }
                
                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(customerId, null, new ArrayList<>());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set for customer ID: {}", customerId);
                
            } catch (Exception e) {
                log.warn("Invalid token: {}", e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}