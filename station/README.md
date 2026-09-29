# Station Service

Station lookup and pricing service for public search, internal station data, and admin activation controls.

## Runtime

- Port: 8000
- Application name: `station`
- Base path: `/api/v1/stations`

## Key Endpoints


### Admin

- `POST /api/v1/admin/stations`
- `PATCH /api/v1/admin/stations/{id}/activate`
- `PATCH /api/v1/admin/stations/{id}/deactivate`
- `PATCH /api/v1/admin/stations/{id}/status`

### Internal

- `GET /api/v1/internal/stations/{id}/price?fuelType=DIESEL`

## Responsibilities

- Stores station metadata and fuel prices.
- Supports public station discovery by city.
- Exposes internal station details for service-to-service calls.

## Configuration

```yaml
server:
  port: 8000

spring:
  application:
    name: station
  datasource:
    url: jdbc:postgresql://localhost:5432/station
```

## Running

```bash
cd station
mvn spring-boot:run
```

## Notes

- Admin station lifecycle endpoints are available under `/api/v1/admin/stations`.
- The controller currently exposes station-by-ID, city lookup, and price lookup.
- There is no mapped public list-all endpoint in the current controller.