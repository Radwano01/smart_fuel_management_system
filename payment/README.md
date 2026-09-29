# Payment Service

Payment processing service for fuel purchases. It performs Stripe pre-authorization, capture, and webhook handling.

## Runtime

- Port: 8100
- Application name: `payment-method`
- Internal API base path: `/api/v1/internal/payments`
- Webhook path: `/api/v1/payments/webhook`

## Key Endpoints

### Internal

- `POST /api/v1/internal/payments/pre-auth`
  - Request: `PaymentRequest { userId, fuelSessionId, amount, currency }`
  - Response: `201 Created` with `PaymentResponse { paymentIntentId, clientSecret, amount }`
- `POST /api/v1/internal/payments/capture`
  - Request: `CaptureRequest { paymentIntentId, amount }`
  - Response: `200 OK` with `CaptureResponse { paymentIntentId, status, capturedAmount }`

### Webhook

- `POST /api/v1/payments/webhook`
  - Accepts Stripe webhook payloads with the `Stripe-Signature` header.

## Responsibilities

- Pre-authorizes and captures card payments.
- Publishes payment state changes for downstream services.
- Validates webhook callbacks from Stripe.
- Coordinates with fuel session and transaction flows.

## Configuration

```yaml
server:
  port: 8100

spring:
  application:
    name: payment-method
  datasource:
    url: jdbc:postgresql://localhost:5432/payment
  jpa:
    hibernate:
      ddl-auto: update

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

payment:
  provider: stripe
  currency: TRY
  retry:
    max-attempts: 3
```

## Running

```bash
cd payment
mvn spring-boot:run
```

## Notes

- The controller lives under `/api/v1/internal/payments`, not the shorter `/internal/payments` path used by older docs.
- Secrets should stay out of the README; use environment variables or local config files instead.
