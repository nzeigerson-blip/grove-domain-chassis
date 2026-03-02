# Grove Domain Chassis Template

Standardized Spring Boot project archetype for all Grove business domains. Handles every non-functional concern so domain developers focus exclusively on business logic.

## What the Chassis Provides

- **Security**: OAuth 2.0 Resource Server, JWT validation, RBAC via `@PreAuthorize`
- **Observability**: Structured JSON logging (correlationId), OpenTelemetry tracing, Prometheus metrics
- **Events**: Kafka producer/consumer with standard `EventEnvelope<T>` and rich payload support
- **Database**: PostgreSQL + Flyway migrations + HikariCP connection pool
- **API Framework**: Standard response/error envelopes, global exception handler, correlation ID propagation
- **Health**: Kubernetes liveness/readiness probes with database connectivity check
- **Audit**: Immutable audit trail on all API requests
- **CI Quality Gate**: JaCoCo 80% coverage enforcement

## Quick Start

```bash
# Clone this template for your domain
git clone <this-repo> grove-{domain-name}
cd grove-{domain-name}

# Delete the sample domain
rm -rf src/main/java/com/grove/sample
rm src/main/resources/db/migration/V2__create_sample_table.sql
mkdir -p src/main/java/com/grove/{domain}/{controller,service,repository,model/{entity,dto,event},exception}

# Update domain name
sed -i 's/sample/{domain}/g' src/main/resources/application.yml

# Run
./gradlew bootRun
```

## Project Structure

```
src/main/java/com/grove/
├── chassis/          # DO NOT MODIFY — managed by chassis template
│   ├── config/       # Security, Kafka, Jackson, OpenTelemetry, Web
│   ├── event/        # EventEnvelope, EventPublisher
│   ├── web/          # ApiResponse, ApiError, GlobalExceptionHandler, CorrelationIdFilter
│   ├── exception/    # GroveException hierarchy
│   ├── audit/        # AuditEvent, AuditLogger, AuditInterceptor
│   ├── logging/      # CorrelationIdContext
│   └── health/       # DomainHealthIndicator
└── {domain}/         # YOUR CODE GOES HERE
    ├── controller/   # REST controllers (thin — delegate to services)
    ├── service/      # Business logic
    ├── repository/   # Data access
    ├── model/
    │   ├── entity/   # JPA entities
    │   ├── dto/      # Request/response DTOs (prefer records)
    │   └── event/    # Kafka event payloads (rich — all owned data)
    ├── exception/    # Domain exceptions (extend GroveException)
    └── audit/        # Domain audit events
```

## Key Conventions

- **Constructor injection only** — all dependencies `final`
- **Java records** for DTOs and event payloads
- **Rich event payloads** — join ALL owned data so consumers don't call back
- **Soft-delete only** — never hard-delete
- **Never log PII** — mask tokens, account numbers, personal data
- **Test naming** — `should_ExpectedBehavior_When_Condition`

## Tech Stack

Java 21 · Spring Boot 3.4.3 · Gradle (Kotlin DSL) · PostgreSQL · Kafka · OpenTelemetry · Prometheus

## Reference

- [Architecture & System Design](https://nzeigerson.atlassian.net/wiki/spaces/SD/pages/622593)
- [Coding Standards & Conventions](https://nzeigerson.atlassian.net/wiki/spaces/SD/pages/655361)
- [API Contracts & Specifications](https://nzeigerson.atlassian.net/wiki/spaces/SD/pages/720897)
