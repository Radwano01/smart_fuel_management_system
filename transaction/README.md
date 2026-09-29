# Transaction Service

Financial audit service that tracks fuel purchase transactions and their payment status.

## Runtime

- Port: 8040
- Application name: `transaction`
- Base path: `/api/v1/transactions`

## Key Endpoints

### User

- `GET /api/v1/transactions`
- `GET /api/v1/transactions/{transactionId}`

### Internal

- `POST /api/v1/internal/transactions/{userId}`
- `PATCH /api/v1/internal/transactions/{transactionId}/status`

## Responsibilities

- Creates and tracks transaction records.
- Synchronizes payment outcomes into transaction status updates.
- Enriches transaction views with station and vehicle information.

## Configuration

```yaml
server:
  port: 8040

spring:
  application:
    name: transaction
  datasource:
    url: jdbc:postgresql://localhost:5432/transaction

transaction:
  bonus-percentage: 5
```

## Running

```bash
cd transaction
mvn spring-boot:run
```

## Notes

- The service uses the payment and fuel session outcomes to update transaction status.
- Transaction history is exposed through the gateway for authenticated users.
