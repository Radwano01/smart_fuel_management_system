# Eureka Service Discovery

Service registry for the microservices in this workspace.

## Runtime

- Port: 8761
- Application name: `eureka-server`
- Type: Spring Cloud Eureka Server

## Responsibilities

- Registers running microservice instances.
- Supports discovery and client-side load balancing.
- Exposes the Eureka dashboard for instance visibility.

## Configuration

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

## Running

```bash
cd eureka
mvn spring-boot:run
```

## Service Registration

Each service points its `eureka.client.service-url.defaultZone` to `http://localhost:8761/eureka`.

## Dashboard

- URL: `http://localhost:8761`
- Shows registered instances, status, and lease information.
