# Vehicle Service

Vehicle registry and lifecycle service for user-owned vehicles, admin controls, and internal validation.

## Runtime

- Port: 8090
- Application name: `vehicle`
- Base path: `/api/v1/vehicles`

## Key Endpoints

### User

- `POST /api/v1/vehicles`
- `GET /api/v1/vehicles`
- `GET /api/v1/vehicles/{vehicleId}`
- `PATCH /api/v1/vehicles/{vehicleId}`
- `PATCH /api/v1/vehicles/{vehicleId}/deactivate`
- `GET /api/v1/vehicles/{vehicleId}/validate`

### Admin

- `PATCH /api/v1/admin/vehicles/{vehicleId}/status`
- `PATCH /api/v1/admin/vehicles/{plateNumber}/rfid`
- `DELETE /api/v1/admin/vehicles/{vehicleId}`
- `GET /api/v1/admin/vehicles/plate/{plateNumber}`

### Internal

- `GET /api/v1/vehicles/internal/{userId}`
- `GET /api/v1/vehicles/internal/{userId}/validate`
- `GET /api/v1/vehicles/internal/resolve`

## Responsibilities

- Tracks vehicle registration, updates, and active status.
- Resolves vehicle identity for fuel session and payment flows.
- Exposes admin routes for lifecycle and RFID management.

## Configuration

```yaml
server:
  port: 8090

spring:
  application:
    name: vehicle
  datasource:
    url: jdbc:postgresql://localhost:5432/vehicle
```

## Running

```bash
cd vehicle
mvn spring-boot:run
```

## Notes

- Public vehicle actions are authenticated and tied to the current user.
- Admin operations are isolated under `/api/v1/admin/vehicles`.
- The legacy RFID controller under `/api/v1/admin/vehicles/rfid` is still available for compatibility.