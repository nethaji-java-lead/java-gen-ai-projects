# TSCNET Daily Process – Spring Boot Demo

A small Senior Full Stack Developer case-study backend demonstrating:

1. Successful automatic initiation
2. Failed automatic initiation
3. Manual initiation after failure
4. Current/historical execution visibility
5. Automated tests
6. Business-day validation
7. Audit-friendly execution records
8. Europe/Berlin scheduler configuration

## Technology

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- H2 for the self-contained demo
- Maven
- JUnit 5

## Run

Prerequisites:
- Java 21+
- Maven 3.9+

Run tests:

```bash
mvn test
```

Run the application:

```bash
mvn spring-boot:run
```

Application:

```text
http://localhost:8080
```

H2 console:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:file:./data/tscnetdb
```

User: `sa`
Password: empty

## REST API

### 1. Successful automatic initiation

Use a weekday:

```bash
curl -X POST "http://localhost:8080/api/process/automatic?businessDate=2026-09-25&simulateFailure=false"
```

Expected status:

```json
"status": "SUCCESS"
```

### 2. Failed automatic initiation

```bash
curl -X POST "http://localhost:8080/api/process/automatic?businessDate=2026-09-28&simulateFailure=true"
```

Expected:

```json
"status": "FAILED"
```

The application logs an operator alert.

### 3. Manual initiation after failure

```bash
curl -X POST "http://localhost:8080/api/process/manual?businessDate=2026-09-28&operator=operator-1&simulateFailure=false"
```

Expected:

```json
"status": "SUCCESS"
```

The failed automatic attempt is retained. The successful manual recovery is recorded separately.

### 4. View current and historical executions

All:

```bash
curl "http://localhost:8080/api/process/executions"
```

For one business date:

```bash
curl "http://localhost:8080/api/process/executions?businessDate=2026-09-28"
```

## Demoing the scheduler

Production configuration is:

```properties
app.scheduler.cron=0 1 0 * * *
app.scheduler.zone=Europe/Berlin
```

That means 00:01 Europe/Berlin every day.

For a live demo, temporarily change the cron to:

```properties
app.scheduler.cron=0 * * * * *
```

This triggers every minute. Restore the production cron before submission.

## Important design decisions

### 1. One initiation service for automatic and manual paths

The scheduler and REST API both call the same service. This avoids duplicating business rules.

### 2. Execution history is append-oriented

A failed automatic attempt is never overwritten by a later manual success. This provides an audit trail.

### 3. Business date is explicit

The business date is stored independently from the execution timestamp.

### 4. Business calendar is isolated

The demo uses Monday-Friday. Production should use an approved regional holiday calendar.

### 5. Notifications are isolated

The demo notification service logs messages. In production it can be replaced by an enterprise notification mechanism without changing the process service.

### 6. Idempotency

The process itself has one record per business date. A unique database constraint prevents multiple process records for the same business date.

## Production improvements

For a production deployment, I would consider:

- PostgreSQL instead of H2
- Enterprise identity provider / OAuth2 / OIDC
- Real notification integration
- Regional holiday calendar
- Distributed scheduler locking if multiple application instances are deployed
- Retry/backoff and dead-letter handling for external dependencies
- Metrics and alerting
- Structured audit logs
- Health checks and observability
- Integration/contract tests
- CI/CD pipeline
- Kubernetes deployment
- Secrets management
