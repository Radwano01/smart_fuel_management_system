package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodDetailsResponse;
import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodRequest;
import com.example.smart_fuel_management_system.entity.PaymentMethod;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import com.example.smart_fuel_management_system.repository.PaymentMethodRepository;
import com.example.smart_fuel_management_system.service.PaymentMethodService;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentMethodAttachParams;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final CustomerService customerService;

    @Override
    public void addPaymentMethod(PaymentMethodRequest request, UUID userId) throws StripeException {

        String customerId = customerService.getOrCreateCustomer(userId);

        PaymentMethod paymentMethod =
                buildPaymentMethod(request, userId, customerId);

        paymentMethodRepository.save(paymentMethod);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentMethodDetailsResponse> getPaymentMethods(UUID userId) {

        return paymentMethodRepository.getByUserId(userId)
                .stream()
                .map(method -> new PaymentMethodDetailsResponse(
                        method.getId(),
                        method.getBrand(),
                        method.getLast4(),
                        method.getExpMonth(),
                        method.getExpYear(),
                        method.isDefault(),
                        isExpired(method.getExpMonth(), method.getExpYear()),
                        method.getCreatedAt(),
                        method.getUpdatedAt()
                ))
                .toList();
    }


    @Override
    @Transactional
    public void setDefault(UUID id, UUID userId) {

        PaymentMethod paymentMethod = paymentMethodRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found"));

        paymentMethodRepository.clearDefaultForUser(userId);

        paymentMethod.setDefault(true);

        paymentMethodRepository.save(paymentMethod);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID userId) throws StripeException {
        String paymentMethodId = paymentMethodRepository.findStripePaymentMethodIdByIdAndUserId(id, userId)
                .orElseThrow(()-> new EntityNotFoundException("Payment Method not found!"));

        com.stripe.model.PaymentMethod stripePaymentMethod =
                com.stripe.model.PaymentMethod.retrieve(paymentMethodId);

        stripePaymentMethod.detach();

        paymentMethodRepository.deleteByIdAndUserId(id, userId);
    }

    private PaymentMethod buildPaymentMethod(
            PaymentMethodRequest request,
            UUID userId,
            String customerId) throws StripeException {

        com.stripe.model.PaymentMethod stripePm =
                com.stripe.model.PaymentMethod.retrieve(request.paymentMethodId());

        stripePm.attach(
                PaymentMethodAttachParams.builder()
                        .setCustomer(customerId)
                        .build()
        );

        com.stripe.model.PaymentMethod.Card card = stripePm.getCard();

        existsCardInSameUser(userId, card.getFingerprint());

        // TODO: Resolve the PaymentProvider dynamically and delegate to the appropriate
        //  PaymentGateway implementation (Stripe, PayPal, Adyen, etc.).
        return PaymentMethod.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .provider(PaymentProvider.STRIPE)
                .providerPaymentMethodId(stripePm.getId())
                .cardFingerPrint(card.getFingerprint())
                .brand(card.getBrand())
                .last4(card.getLast4())
                .expMonth(card.getExpMonth().intValue())
                .expYear(card.getExpYear().intValue())
                .isDefault(existsDefaultPaymentMethod(userId))
                .build();
    }

    private void existsCardInSameUser(UUID userId, String fingerPrint){
        boolean exists = paymentMethodRepository.existsByUserIdAndCardFingerPrint(userId, fingerPrint);

        if(exists){
            throw new EntityExistsException("This card already valid in that account!");
        }
    }

    private boolean isExpired(int expMonth, int expYear) {
        YearMonth now = YearMonth.now();
        YearMonth expiry = YearMonth.of(expYear, expMonth);

        return now.isAfter(expiry);
    }

    private boolean existsDefaultPaymentMethod(UUID userId){
        boolean exists = paymentMethodRepository.existsDefaultByUserId(userId);

        return !exists;
    }
}
