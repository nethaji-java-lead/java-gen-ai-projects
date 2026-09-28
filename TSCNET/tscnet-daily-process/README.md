# TSCNET Daily Process

A Java 21 Spring Boot application for automated daily XML processing of procurement data. The project validates business days, retrieves XML files from SFTP, evaluates procurement offers, persists accepted data, and records execution status for auditability.

## Overview

This project models a business-critical daily process that:

- runs on business days only,
- can be triggered manually or by schedule,
- validates XML before processing,
- routes files to archive or error folders,
- assesses procurement offer quality and thresholds,
- saves accepted data to persistence storage,
- publishes Kafka notifications for process status and operator alerts,
- retains execution history for traceability.

## Features

- Scheduled daily execution with timezone-aware configuration
- Manual REST-triggered processing
- Business day check using weekday logic
- XML validation and parsing
- Procurement quantity and price assessment
- SFTP-based file processing
- Archive/error handling for processed and rejected files
- Process execution history with status tracking
- Kafka event publication for operational notifications

## Technology stack

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Spring Integration + SFTP
- Spring Kafka
- PostgreSQL
- Maven
- JUnit 5 / Spring Boot Test

## Repository structure

```text
.
├── Dockerfile
├── README.md
├── ReadMe.md
├── docker-compose.yml
├── pom.xml
├── sftp/
│   ├── archive/
│   ├── error/
│   └── upload/
├── src/
│   ├── main/java/com/tscnet/dailyprocess/
│   └── main/resources/
│       └── application.properties
└── target/
```

## Prerequisites

Before building or running the project, ensure these tools are available:

- JDK 21 or later
- Maven 3.9+
- PostgreSQL 14+
- Docker + Docker Compose (for local Kafka and SFTP infrastructure)

## Configuration

Application settings are in:

```text
src/main/resources/application.properties
```

Current values in the codebase include:

```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5433/tscnet_daily_process
spring.datasource.username=postgres
spring.datasource.password=... 
spring.jpa.hibernate.ddl-auto=create
app.scheduler.cron=0 1 0 * * *
app.scheduler.enabled=true
app.scheduler.zone=Europe/Munich
sftp.host=localhost
sftp.port=2222
sftp.user=testuser
sftp.password=...
spring.kafka.bootstrap-servers=localhost:9092
```

Update the database, SFTP, and Kafka values to match your local or target environment before running the application.

## Build instructions

From the project root, build the application package:

```bash
mvn clean package
```

This creates the runnable artifact:

```text
target/daily-process-0.0.1-SNAPSHOT.jar
```

## Run locally

### 1) Start required infrastructure

You can start the SFTP and Kafka services defined in Docker Compose:

```bash
docker compose up -d
```

This starts the services declared in `docker-compose.yml`:

- SFTP at `localhost:2222`
- Kafka at `localhost:9092`

### 2) Start PostgreSQL

Make sure PostgreSQL is running and the database exists:

```text
tscnet_daily_process
```

### 3) Launch the Spring Boot app

Run with Maven:

```bash
mvn spring-boot:run
```

Or run the packaged jar:

```bash
java -jar target/daily-process-0.0.1-SNAPSHOT.jar
```

The application starts on:

```text
http://localhost:8081
```

## Docker Compose setup

The included `docker-compose.yml` defines:

### SFTP service

```yaml
services:
  sftp:
    image: atmoz/sftp:latest
    ports:
      - "2222:22"
    volumes:
      - ./sftp/upload:/home/testuser/filesToProcess
      - ./sftp/archive:/home/testuser/archive
      - ./sftp/error:/home/testuser/error
    command: testuser:testpass:1001
```

Credentials:

- username: `testuser`
- password: `testpass`
- port: `2222`

### Kafka service

```yaml
services:
  kafka:
    image: apache/kafka:3.7.0
    ports:
      - "9092:9092"
```

## SFTP and file layout

The application expects the following SFTP directories:

- `filesToProcess` for inbound XML files
- `archive` for successfully processed XML files
- `error` for invalid or rejected XML files

The local Docker mapping is:

```text
./sftp/filesToProcess  -> /home/testuser/filesToProcess
./sftp/archive -> /home/testuser/archive
./sftp/error   -> /home/testuser/error
```

## Scheduling

The scheduler is configured in `application.properties`:

```properties
app.scheduler.cron=0 1 0 * * *
app.scheduler.zone=Europe/Munich
```

This means the daily process is intended to run at 00:01 every day in the Europe/Munich timezone.

For a quick demonstration, the cron may be temporarily changed to a more frequent interval such as every minute:

```properties
app.scheduler.cron=0 * * * * *
```

## API endpoints

### Manual trigger

```bash
curl -X POST "http://localhost:8081/api/process/trigger?businessDate=2026-09-25"
```

This starts processing for a specific business date.

### Failed or partial executions

```bash
curl "http://localhost:8081/api/process/failed"
```

### Latest execution for a date

```bash
curl "http://localhost:8081/api/process/latest?businessDate=2026-09-25"
```

## Processing flow

1. The app checks whether the requested business date is a weekday.
2. It retrieves XML files from the SFTP `filesToProcess` folder.
3. Each XML file is validated.
4. Valid XML is parsed into procurement offer data.
5. The `ProcurementAssessmentService` evaluates:
   - completeness,
   - minimum offer count,
   - total quantity threshold,
   - weighted average price threshold.
6. Accepted files are persisted and moved to `archive`.
7. Rejected or invalid files are moved to `error`.
8. A `ProcessExecutionLog` is saved with success, partial success, or failure status.
9. Kafka notifications are emitted for operational tracking.

## Business-day logic

The implementation in `DateService` treats the following as non-business days:

- Saturday
- Sunday

This intentionally keeps the demo simple. A production system should use a formal regional holiday calendar and more complete business calendar rules.

## Persistence model

The project persists daily process and execution records, including:

- `DailyProcess`
- `ProcessExecutionLog`
- `ProcessFileLog`
- `ProcurementAssessment`
- `ProcurementOffer`

A unique constraint prevents duplicate process entries for the same `business_date`.

## Execution status values

The execution log tracks values such as:

- `IN_PROGRESS`
- `SUCCESS`
- `PARTIAL_SUCCESS`
- `FAILED`
- `ALREADY_EXECUTED`

## Kafka notifications

The application publishes events to Kafka topics including:

- `procurement-assessment-notifications`
- `operator-alerts`

The `NotificationService` is responsible for publication and logging success or failure.

## Testing

Run the test suite:

```bash
mvn test
```

## Troubleshooting

### Maven not found

If `mvn` is unavailable, install Maven and verify:

```bash
mvn -v
```

### PostgreSQL connection errors

Check:

- PostgreSQL is running,
- the database `tscnet_daily_process` exists,
- the port and credentials in `application.properties` match your environment.

### SFTP fails to connect

Check:

- Docker Compose is running,
- port `2222` is open,
- the SFTP username/password matches `testuser/testpass`.

### Kafka fails to connect

Ensure Kafka is started and the application can reach:

```text
localhost:9092
```

## Useful commands

Build the jar:

```bash
mvn clean package
```

Run the app:

```bash
mvn spring-boot:run
```

Start infrastructure:

```bash
docker compose up -d
```

Trigger a manual run:

```bash
curl -X POST "http://localhost:8081/api/process/trigger?businessDate=2026-09-25"
```

Fetch failed executions:

```bash
curl "http://localhost:8081/api/process/failed"
```

## Production hardening considerations

This is a solid demo foundation, but for production you would typically add:

- managed PostgreSQL and secrets management,
- real identity / IAM integration,
- more complete holiday calendars,
- distributed lock handling for multiple app instances,
- retries / DLQ handling for external dependencies,
- observability, metrics, and tracing,
- CI/CD deployment automation,
- stronger audit logging and alerting.