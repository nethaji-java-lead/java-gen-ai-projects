# TSCNET Daily Process – Senior Full Stack Case

## Scope
Reference implementation for the case study: scheduled daily initiation at 00:01 Europe/Berlin, SFTP XML collection, secure XML/XSD validation, archive/error handling, durable execution/file audit, manual recovery, history/current APIs, operator UI and automated tests.

## Important assumptions
1. The case study does not specify the exact downstream initiation API, notification technology, persistence model for the business document, holiday calendar, SFTP key-management mechanism, or authentication provider. These are explicit integration points rather than invented business rules.
2. Saturday/Sunday are treated as non-business days for the demo. Production should use the authoritative TSCNET/market-calendar source, including public holidays.
3. The sample XML is treated as an input contract. The XSD supplied by the business should be placed at `src/main/resources/xsd/balancing_market_document.xsd`; without it the implementation performs secure XML parsing but does not claim XSD compliance.
4. The demo notification adapter logs notifications. Production should connect it to the approved enterprise notification/on-duty mechanism.
5. The SFTP production adapter uses Spring Integration SFTP. A `demo` profile can use local folders for repeatable demonstration.

## Run
`mvn spring-boot:run -Dspring-boot.run.profiles=demo`

UI: `http://localhost:8080/`
History: `GET /api/process/history`
Current: `GET /api/process/current?businessDate=2026-09-25`
Manual: `POST /api/process/trigger?businessDate=2026-09-25`
Health: `GET /actuator/health`

For production, configure SFTP host/user/password or replace password auth with the enterprise SSH key/secret provider. Do not store credentials in source control.

## Demonstration flows
- Successful automatic initiation: put a valid XML into `demo-sftp/filesToProcess`; invoke the scheduled service or call the service in a test; the file moves to `archive/` and the execution is SUCCESS.
- Failed automatic initiation: make SFTP unavailable or put an invalid XML; execution becomes FAILED/PARTIAL_SUCCESS and the operator notification adapter is invoked; invalid files move to `error/`.
- Manual recovery: `POST /api/process/trigger` for the business date after a failed execution.
- Current/history: use `/api/process/current` and `/api/process/history` or the operator UI.

## Production hardening to discuss
- PostgreSQL instead of H2.
- Flyway/Liquibase migrations.
- SFTP SSH keys, host-key verification and secrets manager.
- Distributed lock/leader election if multiple Kubernetes replicas run the scheduler.
- Idempotency using business date + process type and source document mRID/revision.
- Authoritative holiday calendar.
- Structured audit events and immutable audit retention.
- OIDC/RBAC for operators.
- Metrics, traces, dashboards and alerts.
- Outbox/event publication for notifications and downstream integration.
- Object storage/WORM retention if raw XML must be retained for audit.
- Rate limits, file-size limits, malware/content controls and parser resource limits.
