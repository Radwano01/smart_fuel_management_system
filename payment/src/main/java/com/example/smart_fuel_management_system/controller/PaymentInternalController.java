package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.CaptureRequest;
import com.example.smart_fuel_management_system.dto.CaptureResponse;
import com.example.smart_fuel_management_system.dto.PaymentRequest;
import com.example.smart_fuel_management_system.dto.PaymentResponse;
import com.example.smart_fuel_management_system.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/payments")
@RequiredArgsConstructor
public class PaymentInternalController {

    private final PaymentService paymentService;


    @PostMapping("/pre-auth")
    public ResponseEntity<PaymentResponse> preAuth(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.preAuth(request));
    }


    @PostMapping("/capture")
    public ResponseEntity<CaptureResponse> capture(@Valid @RequestBody CaptureRequest request){
        return ResponseEntity.ok(paymentService.capturePayment(request));
    }
}