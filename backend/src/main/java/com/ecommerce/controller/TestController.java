package com.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> testAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "message", "No authentication found"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "customerId", authentication.getName(),
            "message", "Authentication successful"
        ));
    }
    
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> testPublic() {
        return ResponseEntity.ok(Map.of(
            "message", "Public endpoint working",
            "status", "success"
        ));
    }
}