package com.example.smart_fuel_management_system.controller;


import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodDetailsResponse;
import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodRequest;
import com.example.smart_fuel_management_system.service.PaymentMethodService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<Void> addPaymentMethod(@Valid @RequestBody PaymentMethodRequest request, Principal principal) throws StripeException {
        UUID userId = UUID.fromString(principal.getName());
        paymentMethodService.addPaymentMethod(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodDetailsResponse>> getPaymentMethods(Principal principal){
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(paymentMethodService.getPaymentMethods(userId));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<Void> setDefault(
            @PathVariable UUID id,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());
        paymentMethodService.setDefault(id, userId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Principal principal) throws StripeException {
        UUID paymentMethodId = UUID.fromString(id);
        UUID userId = UUID.fromString(principal.getName());
        paymentMethodService.delete(paymentMethodId, userId);
        return ResponseEntity.noContent().build();
    }
}
