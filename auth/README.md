# Auth Service

Authentication service for login, OTP verification, JWT issuance, password reset, and password change flows.

## Runtime

- Port: 8888
- Base path: `/api/v1/auth`
- Application name: `auth`
- Persistence: PostgreSQL, Redis, RabbitMQ

## Key Endpoints

### Public

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/verify-otp`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/refresh`
- `POST /api/v1/auth/forgot-password`
- `PATCH /api/v1/auth/reset-password`
- `PATCH /api/v1/auth/change-password`
- `POST /api/v1/auth/resend`

## What It Does

- Hashes passwords and issues short-lived JWTs.
- Uses OTP flows for new registration and recovery.
- Publishes notification events for email delivery.
- Uses Redis for temporary auth state and rate limiting.

## Configuration

```yaml
server:
  port: 8888

spring:
  application:
    name: auth
  datasource:
    url: jdbc:postgresql://localhost:5432/auth
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672

jwt:
  expiration: 900000
```

## Running

```bash
cd auth
mvn spring-boot:run
```

## Notes

- The existing controller exposes OTP verification as part of the auth flow.
- Password reset and resend routes are part of the public auth surface.
