package com.example.smart_fuel_management_system.config;

import com.example.smart_fuel_management_system.dto.StripeConfigResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeConfigService {

    @Value("${stripe.public-key}")
    private String publishableKey;

    public StripeConfigResponse getConfig() {
        return new StripeConfigResponse(publishableKey);
    }
}