package com.ecommerce.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Configuration
@Slf4j
public class LoggingConfig {

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    public static class RequestLoggingFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            String traceId = UUID.randomUUID().toString().substring(0, 8);
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String remoteAddr = getClientIpAddress(request);
            
            MDC.put("traceId", traceId);
            
            long startTime = System.currentTimeMillis();
            
            log.info("REQUEST_START - {} {} {} - Client: {} - TraceId: {}", 
                    method, uri, queryString != null ? "?" + queryString : "", remoteAddr, traceId);
            
            try {
                filterChain.doFilter(request, response);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                int status = response.getStatus();
                
                log.info("REQUEST_END - {} {} - Status: {} - Duration: {}ms - TraceId: {}", 
                        method, uri, status, duration, traceId);
                
                if (duration > 5000) {
                    log.warn("SLOW_REQUEST - {} {} took {}ms - TraceId: {}", method, uri, duration, traceId);
                }
                
                MDC.clear();
            }
        }
        
        private String getClientIpAddress(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            return request.getRemoteAddr();
        }
    }
}