package com.example.smart_fuel_management_system.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {

        return exchange -> {

            String ip = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");

            if (ip == null || ip.isBlank()) {
                ip = Objects.requireNonNull(exchange.getRequest()
                                .getRemoteAddress())
                        .getAddress()
                        .getHostAddress();
            }

            ip = ip.split(",")[0].trim();

            try {
                if (InetAddress.getByName(ip).isLoopbackAddress()) {
                    ip = "localhost";
                }
            } catch (Exception ignored) {
            }

            return Mono.just(ip);
        };
    }
}