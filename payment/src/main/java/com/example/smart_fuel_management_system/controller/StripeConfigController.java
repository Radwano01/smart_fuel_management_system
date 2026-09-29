package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.config.StripeConfigService;
import com.example.smart_fuel_management_system.dto.StripeConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class StripeConfigController {

    private final StripeConfigService stripeConfigService;

    @GetMapping("/config")
    public StripeConfigResponse getConfig() {
        return stripeConfigService.getConfig();
    }
}