# Decisions and Trade-offs

---

# Event-Api (Java)

### Data Layer Access with JOOQ

The original code used Spring JPA to access the Postgres database. I switched to JOOQ for a more explicit and flexible data access layer. JOOQ allows me to write SQL-like queries in Java, which is more efficient and behaviour is explicit compared to JPA's abstraction. This is especially beneficial for the stats and event listing endpoints, where I can do complex queries without worrying about JPA's n+1 issues or lazy loading surprises.

Pros:
- Personal preference for explicit SQL queries and transparent data access.
- Better performance and control over queries.
- Easier to optimise and maintain complex queries for stats and pagination.

Cons:
- More boilerplate compared to JPA's repository pattern.
- Less abstraction, which leads to more verbose code for simple CRUD operations.
- Requires manual mapping of database records to Java objects, which can be error-prone.

### Flyway for Migrations

I added Flyway for database migrations to ensure the schema is version-controlled and can be consistently applied across environments. `init.sql` was replaced with a Flyway migration script (`V1__events.sql`) to keep all migrations in one place.

Pros:
- Version control for database schema changes.
- Easy to apply and roll back migrations across environments.
- Personal preference: lightweight and well-understood solution.

Cons:
- Adds another dependency to the project.
- Depending on the CD pipeline, migrations may need to run as a separate step before deployment.

### Testcontainers for Integration Tests

I used Testcontainers to run a real Postgres instance for integration tests. This ensures that queries and data access code work correctly in a realistic environment — particularly important for the stats endpoint where query correctness matters.

Pros:
- Tests run against a real database, providing high confidence in data access correctness.
- Atomic setup and teardown per test run ensures a clean state.
- Personal preference: I prefer testing against real dependencies rather than mocks for integration tests.

Cons:
- Slower test execution compared to in-memory databases or mocks.
- Requires Docker to be available in the test environment.

### Kafka Outbox Pattern

I implemented a custom transactional outbox pattern to ensure reliable event publishing to Kafka. The solution combines an immediate dispatch path (via `@TransactionalEventListener` after commit) with scheduled polling for retries and stuck event recovery. Events are claimed, processed, and marked as completed in a way that ensures at-least-once delivery without duplicates, even in failure scenarios.

Pros:
- Reliable event publishing with at-least-once delivery guarantees.
- Handles retries and stuck events gracefully — events are never silently lost.
- Personal preference: I prefer a custom implementation I can fully understand and control over an opaque external library.

Cons:
- More complex than necessary for simple use cases.
- Requires careful handling of transactions and concurrency.
- Ready-made alternatives exist (Debezium, Spring Cloud Stream) that may be simpler to operate for some teams.

### Spring Security with JWT

I added Spring Security to secure the API endpoints with JWT-based authentication. Stateless authentication with a role hierarchy (`ADMIN > USER`).

Hardcoded users:
- `admin@example.com` / `admin123` — `ROLE_ADMIN`
- `user@example.com` / `password` — `ROLE_USER`

Pros:
- Flexible and secure authentication with JWT tokens.
- Standard approach with broad ecosystem support.

Cons:
- In hindsight, Spring Security is heavy for this scope and adds significant configuration complexity.
- Verbose and "black box" behaviour around exceptions and access rules.
- Personal preference: for a task of this size, a simple custom request interceptor checking a static token would have been more appropriate.

---

# Event-Processor (Python)

### HTTP Status API & Event Registry

The task required `GET /events/{id}/status` to return the Minio `objectKey` for a processed event. 
The original skeleton had no link between a consumed Kafka message and its stored Minio object and after upload, the event ID was forgotten.

I introduced an in-memory `EventRegistry` (a thread-safe `dict` wrapped in a `threading.Lock`) mapping `event_id → EventRecord`. 
The consumer marks an event `pending` immediately on consume, then `processed` (with the object key and timestamp) only after a Minio write. 
If the upload fails, status stays `pending`.

```
GET /events/{id}/status

404                                          → event never seen by the processor
{"status": "pending"}                        → consumed from Kafka, upload not yet complete
{"status": "processed",                      → transformed and stored in Minio
 "objectKey": "b7743207-....xml",
 "processedAt": "2026-05-10T12:34:56Z"}
```

Pros:
- Zero extra infrastructure.
- Object key is deterministic (`{event_id}.xml`), making reprocessing idempotent.

Cons:
- State is lost on process restart. Mitigated by manual offset commits (`enable.auto.commit=False`).
- For true durability across restarts leverage Redis or alternative.

### Health Check

I implemented real dependency probes rather than a liveness-only check.

- **Kafka** — `AdminClient.list_topics(timeout=3)` forces a broker metadata round-trip.
- **Minio** — `bucket_exists("healthcheck")` issues an authenticated `HEAD` request, validating both connectivity and credentials without needing the bucket to exist.

Both probes run on every request and errors are collected into a list, so a single call reveals all failing dependencies at once.

```
GET /health

{"status": "ok"}                             → 200, all dependencies reachable
{"status": "unhealthy",                      → 503
 "errors": ["kafka: ...", "minio: ..."]}
```

Pros:
- Reflects actual service health.
- Aggregated error list.

Cons:
- Adds two network round-trips to every `/health` call.