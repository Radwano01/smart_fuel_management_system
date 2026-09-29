# User Service

User profile service for registration, current-user profile management, password changes, and internal lookups.

## Runtime

- Port: 8200
- Application name: `user`
- Base path: `/api/v1/users`

## Key Endpoints

### Public

- `POST /api/v1/users/register`
- `POST /api/v1/users/login`
- `POST /api/v1/users/password-reset/request`
- `POST /api/v1/users/password-reset`

### Authenticated

- `GET /api/v1/users`
- `PATCH /api/v1/users`
- `PATCH /api/v1/users/password`

### Internal

- `GET /api/v1/users/internal/{userId}`

## Responsibilities

- Stores the user profile and account metadata.
- Exposes the authenticated user record for the gateway and downstream services.
- Uses JWT bearer authentication for protected routes.
- Consumes the shared auth identity and publishes user-created events.

## Configuration

```yaml
server:
  port: 8200

spring:
  application:
    name: user
  datasource:
    url: jdbc:postgresql://localhost:5432/users
  data:
    redis:
      host: localhost
      port: 6379
```

## Running

```bash
cd user
mvn spring-boot:run
```

## Notes

- Login uses phone number rather than email.
- The current controller exposes the authenticated user at `GET /api/v1/users`.