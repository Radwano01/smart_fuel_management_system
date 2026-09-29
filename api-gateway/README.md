# API Gateway Service

Single entry point for client requests. It routes traffic to the domain services and applies shared security and rate limiting policies.

## Runtime

- Port: 8080
- Application name: `api-gateway`
- Discovery: Eureka

## Key Routes

| Path | Routes To |
|------|-----------|
| `/api/v1/users/**` | User Service |
| `/api/v1/auth/**` | Auth Service |
| `/api/v1/vehicles/**` | Vehicle Service |
| `/api/v1/stations/**` | Station Service |
| `/api/v1/transactions/**` | Transaction Service |
| `/api/v1/fuel-sessions/**` | Fuel Session Service |
| `/api/v1/payments/**` | Payment Service |

## Responsibilities

- Routes public and protected API traffic to the correct service.
- Validates JWT-backed requests before forwarding protected calls.
- Applies cross-cutting concerns such as CORS and request throttling.

## Configuration

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER
          predicates:
            - Path=/api/v1/users/**
```

## Running

```bash
cd api-gateway
mvn spring-boot:run
```

## Health Check

```bash
curl http://localhost:8080/actuator/health
```
