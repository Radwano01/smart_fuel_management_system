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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

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

            String role = jwtService.extractRole(token);

            UsernamePasswordAuthenticationToken authentication;

            switch (role) {

                case "STATION" -> {

                    UUID employeeId = jwtService.extractEmployeeId(token);

                    authentication = new UsernamePasswordAuthenticationToken(
                            employeeId,
                            null,
                            List.of(new SimpleGrantedAuthority("STATION"))
                    );
                }

                case "ADMIN" -> {

                    String adminId = jwtService.extractSubject(token);

                    authentication = new UsernamePasswordAuthenticationToken(
                            adminId,
                            null,
                            List.of(new SimpleGrantedAuthority("ADMIN"))
                    );
                }

                case "SERVICE" -> {

                    if (!List.of("fuel-session-service", "transaction-service", "auth-service", "dashboard-service").contains(jwtService.extractIssuer(token))) {
                        throw new RuntimeException("Invalid issuer");
                    }

                    if (!"station-service".equals(jwtService.extractAudience(token))) {
                        throw new RuntimeException("Invalid audience");
                    }

                    String serviceName = jwtService.extractSubject(token);

                    authentication = new UsernamePasswordAuthenticationToken(
                            serviceName,
                            null,
                            List.of(new SimpleGrantedAuthority("SERVICE"))
                    );
                }

                default -> throw new RuntimeException("Unknown token type");
            }

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}