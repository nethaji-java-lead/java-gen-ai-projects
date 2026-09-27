# TSCNET Daily Process – Detailed Solution Design

## 1. Case interpretation
The assessment describes a business-critical daily regional procurement process with collection, validation, processing, business calculations, operator review/exception handling, recommendation generation, notification/reporting and external/internal integrations. It explicitly requires Java, Kubernetes, security/auditability, an operator UI, and a working implementation demonstrating successful automatic initiation, failed automatic initiation, manual initiation after failure, and current/historical execution visibility.

## 2. Proposed architecture
```text
                         +-----------------------------+
                         | Operator UI (HTML/React)    |
                         +--------------+--------------+
                                        |
                                  REST / OIDC
                                        v
+-------------+        +---------------+----------------+
| Scheduler   |------->| Process Orchestrator            |
| 00:01 CET   |        | business date / idempotency     |
+-------------+        | execution state / audit         |
                       +-------+-------------+------------+
                               |             |
                         SFTP adapter     Notification
                               |             |
                               v             v
                     +----------------+   +----------------+
                     | External SFTP  |   | On-duty /      |
                     | filesToProcess |   | participants   |
                     +-------+--------+   +----------------+
                             |
                      secure XML parser
                             |
                      XSD validation
                             |
                 +-----------+------------+
                 |                         |
              valid                     invalid
                 |                         |
             processing                 error/
                 |                         |
              archive/                  audit
                 |
           PostgreSQL / audit DB
                 |
          metrics / traces / logs
                 |
             Kubernetes
```

## 3. Core lifecycle
`SCHEDULED -> IN_PROGRESS -> SUCCESS | PARTIAL_SUCCESS | FAILED`

File lifecycle: `filesToProcess -> PROCESSING -> archive | error`.

A business date has one durable process execution. A successful execution is idempotent: another trigger for the same business date returns the existing successful result rather than initiating the process again. Failed executions may be retried manually.

## 4. Why this improves the supplied prototype
Your prototype already has the important SFTP + validator + scheduler + manual REST flow. I would explicitly fix these points before presenting it:

- **Business date:** do not derive the process date implicitly from `LocalDate.now()` in every layer. Pass a resolved Europe/Berlin business date through the workflow.
- **Weekend logic:** your current `isBusinessDay()` excludes Sunday only. The case says “business day”; Saturday must not be assumed to be a business day. Production needs the authoritative holiday/calendar service.
- **Duplicate execution:** the original code can start the same date twice if scheduler and operator act concurrently. A DB unique constraint plus a distributed lock/leader-election mechanism is required for Kubernetes.
- **Transaction boundary:** do not keep a database transaction open across a slow SFTP operation in production. Prefer short state-transition transactions and per-file durable audit records.
- **Counts:** calculate counts from persisted file records, not an in-memory entity that may not represent independently committed work.
- **Failure semantics:** distinguish process-initiation failure, SFTP failure, XML parsing/XSD failure, domain-processing failure and notification failure.
- **Notifications:** notification failure should be observable and retryable; it should not silently erase the process result.
- **Security:** use SSH host-key verification, secret management, least-privilege SFTP account, secure XML parser settings, operator authentication/RBAC and audit logging.
- **Observability:** expose health, metrics, structured logs and correlation/execution IDs.

## 5. XML handling
The supplied sample is an IEC 62325 Balancing_MarketDocument with `mRID`, revision, process type, sender/receiver, created timestamp, area and TimeSeries/Period/Point data. The sample shows 15-minute resolution and quantities/prices. The implementation deliberately does not invent business calculations from these fields. Once the authoritative XSD and business rules are supplied, the validated XML should be mapped to domain objects and processed by a separate domain service.

## 6. Idempotency strategy
Primary key for process initiation: `(businessDate, processType)`.

Document-level key: `(documentMrid, revisionNumber)` plus sender/receiver where required by the business contract.

If a file is delivered twice under different filenames, the XML document identifier—not the filename—should determine duplicate handling. A production design should persist the checksum as an additional integrity/deduplication signal.

## 7. Kubernetes considerations
Run the application statelessly except for external PostgreSQL/SFTP. Multiple replicas are expected. Only one replica should own the scheduled initiation. Options: Kubernetes leader election, ShedLock backed by PostgreSQL, or an enterprise scheduler. The demo uses Spring scheduling because the assessment asks for a small executable implementation; the production decision must explicitly address duplicate scheduler execution.

## 8. Security
- SFTP: SSH keys, verified host key, dedicated account, restricted directory and permissions.
- Secrets: Kubernetes Secret integrated with enterprise vault; never source-controlled.
- XML: disable DTD/external entities/external schema access; enforce maximum file size and processing time.
- API: OIDC/OAuth2, operator roles, audit every manual trigger.
- Database: encrypted transport/storage, least-privilege service account.
- Audit: retain execution, user, timestamps, file name, destination, document mRID, outcome and error details according to retention policy.

## 9. Testing strategy
Unit: business-day resolution, state transitions, XML security, validation mapping.
Integration: SFTP read/move, database persistence, duplicate execution, manual retry.
Contract: XML/XSD fixtures and downstream API contracts.
End-to-end: scheduler success, scheduler failure, operator recovery, history/UI.
Resilience: SFTP unavailable, archive move failure, DB unavailable, notification failure, duplicate trigger, malformed XML, oversized XML.

## 10. Nine-month delivery approach
Months 1–2: discovery, unknowns, contracts, security model, architecture, SFTP/XSD proof of concept.
Months 3–4: ingestion, validation, audit, process orchestration, operator APIs/UI.
Months 5–6: downstream integrations, calculations, notifications, observability, security testing.
Month 7: integration/UAT, failure-mode testing, operational runbooks.
Month 8: performance, DR, penetration/security remediation, operator training.
Month 9: production readiness, controlled rollout, knowledge transfer and vendor-to-internal handover.

## 11. Key unknowns to state in the interview
1. Exact business-day/holiday calendar.
2. What “initiate process” means technically: downstream API, workflow engine, message, database procedure, etc.
3. Required XML XSD versions and validation rules.
4. Expected file volume/size and arrival window.
5. Duplicate/revision semantics.
6. SFTP authentication, host-key and folder conventions.
7. Notification/on-duty tooling and escalation SLA.
8. Identity provider and operator roles.
9. Required audit/retention period.
10. Existing applications/APIs and ownership boundaries.
11. RTO/RPO and DR requirements.
12. Exact business calculations and recommendation rules.

## 12. Trade-offs / scope
Keep in first release: reliable daily initiation, SFTP ingestion, XML/XSD validation, durable audit, manual recovery, operator UI, RBAC, notifications, monitoring, Kubernetes deployment.
Phase later if not required for go-live: advanced analytics, complex workflow visualisation, multi-region active-active, AI-based anomaly detection, highly elaborate UI customisation.

## 13. Acceptance criteria
- At 00:01 Europe/Berlin on a business day, exactly one process initiation is attempted for that business date.
- A successful initiation is durable and visible in current/history views.
- If automatic initiation fails, the failure is durable and an on-duty notification is emitted.
- An authenticated operator can manually retry a failed business date.
- Manual retry is auditable with user and timestamp.
- XML files are read from SFTP, securely parsed, validated and moved to archive on success or error on failure.
- Every file has a durable status and error/destination information.
- Repeated successful triggers do not create duplicate successful process executions.
- Automated tests cover the key business and failure paths.
