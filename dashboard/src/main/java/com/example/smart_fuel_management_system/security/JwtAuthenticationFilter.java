package com.example.smart_fuel_management_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtService.validateToken(token)) {
                throw new RuntimeException("Invalid token");
            }

            String tokenType = jwtService.extractRole(token);

            Object principal;
            List<SimpleGrantedAuthority> authorities;

            switch (tokenType) {

                case "ADMIN" -> {
                    principal = jwtService.extractSubject(token);

                    authorities = List.of(
                            new SimpleGrantedAuthority("ADMIN")
                    );
                }

                case "SERVICE" -> {
                    principal = jwtService.extractSubject(token);

                    authorities = List.of(
                            new SimpleGrantedAuthority("SERVICE")
                    );
                }

                case "USER" -> {
                    principal = jwtService.extractUserId(token);

                    authorities = List.of(
                            new SimpleGrantedAuthority("USER")
                    );
                }

                default -> throw new RuntimeException("Unknown token type");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            logger.error("JWT authentication failed", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}