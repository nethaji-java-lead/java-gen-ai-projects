# TSCNET Daily Process — demo implementation

## User story
As the on-duty TSCNET operator, I want the daily regional procurement process to start automatically at 00:01 Europe/Munich on each business day, with a recorded outcome and notification, so that participants can continue processing and I can investigate failures and recover manually with a full execution history.

## Acceptance criteria
1. On weekdays at 00:01 Europe/Munich, the scheduler attempts initiation for the local business date.
2. Each attempt records execution ID, business date, trigger type, status, timestamps, and outcome message.
3. A successful initiation returns `SUCCESS` and emits an outcome notification through the notification adapter.
4. A failed attempt is persisted as `FAILED` and appears in `GET /api/process/failed`.
5. After a failure, an operator can retry manually; the manual trigger and outcome are recorded as a separate execution.
6. A successful or in-progress execution prevents duplicate initiation for the same business date.
7. Current and historical executions are available through REST endpoints.
8. Automated tests cover success, failure/manual recovery, idempotency, and history/failed queries.

## Architecture and scope
Java 21, Spring Boot, Spring Data JPA, H2 file database, REST API, weekday scheduler, and a notification adapter. The adapter logs notifications; replace it with Kafka in a production deployment. This demo's process body simulates initiation rather than implementing external XML/SFTP procurement assessment. The scheduler uses weekdays only, not a formal regional holiday calendar.

## Build and run
Prerequisites: JDK 21 and Maven 3.9+.

```bash
mvn clean test
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. The database is stored under `./data/`.

## API examples
Trigger a successful manual execution:
```bash
curl -X POST 'http://localhost:8080/api/process/trigger?businessDate=2026-09-28&triggerType=MANUAL'
```

Simulate automatic failure:
```bash
curl -X POST 'http://localhost:8080/api/process/trigger?businessDate=2026-09-29&triggerType=AUTO&simulateFailure=true'
```

Retry that date manually:
```bash
curl -X POST 'http://localhost:8080/api/process/trigger?businessDate=2026-09-29&triggerType=MANUAL'
```

List failed executions, latest execution, or all history:
```bash
curl 'http://localhost:8080/api/process/failed'
curl 'http://localhost:8080/api/process/latest'
curl 'http://localhost:8080/api/process/history'
curl 'http://localhost:8080/api/process/history/2026-09-29'
```

## Automated tests
`ProcessServiceTest` uses Spring Boot Test, JUnit 5, AssertJ and a mocked notification adapter. It checks successful initiation, failure followed by manual recovery, duplicate prevention, and historical/failed execution visibility.

## Production hardening still required
- Implement SFTP file collection, XML XSD validation, business rules and procurement assessment.
- Replace logging notification adapter with Kafka producer and define retry/DLQ semantics.
- Add distributed scheduler locking for multi-replica Kubernetes deployments.
- Add authentication/authorization, secrets management, structured metrics/tracing, alerting and operational runbooks.
- Confirm business-day/holiday calendar and daylight-saving expectations with stakeholders.

## Notes
The assessment deck describes Kafka, SFTP and PostgreSQL as the target architecture; this runnable slice keeps external integration boundaries explicit but uses a local H2 database and log-based notification adapter for a small executable demonstration.
