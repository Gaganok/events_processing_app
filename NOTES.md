# Event Api (Java) notes

## Decisions and trade-offs

# Event-Api (Java)

### Data Layer Access with JOOQ
The original code used Spring JPA to access the Postgres database. 
I switched to JOOQ for a more explicit and flexible data access layer. 
JOOQ allows me to write SQL-like queries in Java, which can be more efficient and behaviour is explicit compared to JPA's abstraction.
This is especially beneficial for the stats and event listing endpoints, where I can do complex db calls without worrying about JPA's n+1 issues or underwater lazy loading.

Pros:
- Personal Preference for explicit SQL queries and transparent data access.
- Better performance and control over queries.
- Easier to optimise/maintain complex queries for stats and pagination.

Cons:
- More boilerplate code compared to JPA's repository pattern.
- Less abstraction, which can lead to more verbose code for simple CRUD operations.
- Requires manual mapping of database records to Java objects, which can be error-prone.

### Flyway for Migrations
I added Flyway for database migrations to ensure that the database schema is version-controlled and can be easily applied across different environments. 
This is a standard practice for managing database changes and ensures that the schema is consistent and can be rolled back if necessary.

init.sql replaced with a Flyway migration script (V1__events.sql) to set up the initial database schema and keep migrations together in one place.

Pros:
- Version control for database schema changes.
- Easy to apply and roll back migrations across environments.
- Personal preference: easy and lightweight solution for managing database schema.

Cons:
- Add another dependency to the project.
- Might be complications depending on the CD pipeline (migrations may need to run separately).

### Test container for integration tests
I used Test containers to run a Postgres instance for integration tests.
This allows me to test against a real database instance, ensuring that the queries and data access code work as expected in a realistic environment.
This is especially important for the stats endpoint, where I want to ensure that the queries perform well and return correct results.

Pros:
- Tests run against a real database instance, providing more confidence in the correctness of the data access code.
- Atomic setup and tear down of the database for each test run, ensuring a clean state.
- Easy to set up and tear down for tests, ensuring a clean state for each test run.
- Personal preference: I prefer testing against real dependencies rather than mocks for integration tests.

Cons:
- Slower test execution compared to in-memory databases or mocks.
- Adds complexity to the test setup and requires Docker to be available for running tests.

### Kafka Outbox Implementation
I have implemented a custom transactional outbox pattern to ensure reliable event publishing to Kafka.
The solution combines an immediate dispatch path (via TransactionalEventListener after commit) 
with scheduled polling for retries and stuck event recovery. 
Events are claimed, processed, and marked as completed in a way that ensures at-least-once delivery without duplicates, even in failure scenarios.
This approach provides a robust and scalable solution for event publishing while maintaining data integrity and consistency.

Pros:
- Ensures reliable event publishing to Kafka with at-least-once delivery guarantees.
- Handles retries and stuck events gracefully, ensuring that events are not lost or duplicated.
- Personal preference: I prefer a custom implementation that I can control and understand fully, rather than relying on external libraries or frameworks.

Cons:
- Might be too complex for simple use cases where a simpler solution might suffice.
- Requires careful handling of transactions and concurrency to avoid issues with event processing.
- Ready solutions also available like Debezium or Spring Cloud Stream with Kafka, which might be easier to set up and maintain for some teams.

### Spring Security
I added Spring Security to secure the API endpoints with JWT token based authentication.
This is a common and robust approach to securing APIs, allowing for stateless authentication and easy integration with various identity providers.
Personally would prefer to use cookie session based authentication instead of Authentication header.

Added Role Hierarchy:
Admin > User

Hardcoded users:
- username: admin@example.com / password: admin123 (ROLE_ADMIN)
- username: user@example.com / password: password (ROLE_USER)

Pros:
- Allow flexible and secure authentication mechanism with JWT tokens.
- Various options for token generation and validation.

Cons:
- Made a mistake picking spring security for this task, as it is a bit heavy weight for this use case and adds complexity to the codebase.
- Complex and verbose configuration, especially for a simple API like this.
- Too "black box" when dealing with exceptions and setting up rules.
- Personal preference: I would have preferred a simpler authentication mechanism, maybe event custom request interceptor that checks for a static token or something similar, given the scope of this project.