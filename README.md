# Smart Fuel Management System

Microservices-based fuel management platform built with Spring Boot, Spring Cloud, PostgreSQL, Redis, and RabbitMQ.

## Architecture

```text
Client -> API Gateway -> Domain services -> Database / Message broker
                   \-> Eureka for service discovery
```

## Modules

| Module | Port | Purpose |
|--------|------|---------|
| [api-gateway](api-gateway/README.md) | 8080 | Central entry point and request routing |
| [eureka](eureka/README.md) | 8761 | Service discovery server |
| [user](user/README.md) | 8200 | User profile, authentication context, and account data |
| [auth](auth/README.md) | 8888 | Login, OTP verification, password reset, JWT issuance |
| [vehicle](vehicle/README.md) | 8090 | Vehicle registration and lifecycle management |
| [station](station/README.md) | 8020 | Fuel station lookup and pricing |
| [fuelSession](fuelSession/README.md) | 8250 | Internal fuel session lifecycle |
| [transaction](transaction/README.md) | 8040 | Transaction tracking and status updates |
| [payment](payment/README.md) | 8100 | Payment pre-authorization, capture, and webhooks |
| [notification](notification/README.md) | n/a  | RabbitMQ-driven email and SMS notifications |

## Quick Start

```bash
mvn clean package -DskipTests
docker-compose up -d
```

To run one service locally:

```bash
cd <service-name>
mvn spring-boot:run
```

## Service Notes

- The gateway exposes the public API surface.
- Eureka handles registration and discovery for the Spring Cloud services.
- Notification is consumer-only and does not expose HTTP endpoints.
- The workspace does not contain separate wallet, payment-method, or payment-preference modules.
