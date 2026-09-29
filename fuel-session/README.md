# FuelSession Service

Internal fuel session lifecycle service for starting and stopping fueling sessions.

## Runtime

- Port: 8250
- Application name: `fuel_session`
- Base path: `/api/v1/fuel-sessions/internal`

## MQTT Topics

- `fuel/session/start`
- `fuel/session/stop`
- `fuel/session/start/response/{pumpId}`
- `fuel/session/stop/response/{pumpId}`

## Responsibilities

- Starts sessions after vehicle, station, and payment checks pass.
- Completes sessions and records consumption totals.
- Publishes success or error responses back to the dispenser topic.

## Configuration

```yaml
server:
  port: 8250

spring:
  application:
    name: fuel_session
  datasource:
    url: jdbc:postgresql://localhost:5432/fuel_session
```

## Running

```bash
cd fuelSession
mvn spring-boot:run
```

## Notes

- The dispenser integration now uses MQTT only.
- REST/gRPC calls to Vehicle, Station, and Payment remain unchanged.
- This service still coordinates vehicle, station, transaction, and payment flows.