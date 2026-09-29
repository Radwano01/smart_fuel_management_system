package com.example.smart_fuel_management_system.security;

import com.example.smart_fuel_management_system.rateLimit.RateLimitRule;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisRateLimiter rateLimiter;

    public RateLimitingFilter(RedisRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String user = request.getUserPrincipal() != null
                ? request.getUserPrincipal().getName()
                : request.getRemoteAddr();

        RateLimitRule rule = RateLimitRule.fromPath(path, request.getMethod());

        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String scope = path.startsWith("/api/v1/payments/admin")
                ? "ADMIN"
                : "PUBLIC";

        String key = scope + ":" + path + ":" + user;

        if (!rateLimiter.isAllowed(
                key,
                rule.capacity,
                rule.duration.getSeconds()
        )) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return;
        }

        filterChain.doFilter(request, response);
    }
}