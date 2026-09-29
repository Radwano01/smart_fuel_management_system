# Notification Service

Event-driven notification service for email and SMS delivery.

## Runtime

- Type: consumer-only service
- HTTP port: none
- Messaging: RabbitMQ
- Email: SMTP via JavaMailSender

## Queues

- `notification.email.queue` - sends OTP emails
- `notification.reset.password.queue` - sends password reset emails
- `notification.phone.number.queue` - reserved for SMS notifications

## Responsibilities

- Sends OTP email messages.
- Sends password reset emails with a link back to the auth service.
- Consumes SMS notification events for future or external SMS integration.

## Configuration

```yaml
spring:
  application:
    name: notification
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  mail:
    host: smtp.gmail.com
    port: 587
```

## Running

```bash
cd notification
mvn spring-boot:run
```

## Notes

- The service does not expose REST endpoints.
- The password reset email currently points back to the auth service reset route.