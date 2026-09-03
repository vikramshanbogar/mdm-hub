# MDM Hub (Spring Boot 3.5, JDK 21, JPA, PostgreSQL)

A clean, modern Master Data Management (MDM) hub starter — a fresh
Spring Boot rewrite of the classic "hub" pattern (golden records, source
cross-references, merge/survivorship) using **embedded Tomcat**, not
a JBoss/JEE container. No app server install, no Oracle — just
`mvn spring-boot:run` against Postgres.

## Stack

- Java 21
- Spring Boot 3.5.x (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`)
- Spring Data JPA + Hibernate, PostgreSQL driver
- springdoc-openapi (Swagger UI) for interactive API docs
- Maven
- Docker / Docker Compose, plus example Kubernetes manifests

> Spring Boot 4.1 is the newest major line as of mid-2026 (Spring Framework 7,
> Jackson 3, renamed/modularized starters). This scaffold targets the more
> battle-tested 3.5 line so you're not fighting framework migration issues
> while you're getting the domain code working — upgrading later is a
> reasonable next step once you're comfortable.

## Domain model

- **SourceSystem** — an upstream system that feeds records in (CRM, ERP, etc.)
- **Party** — the golden/master record for a person or organization
- **PartyCrossReference** — traces a golden `Party` back to a specific record
  id in a given `SourceSystem` (the classic MDM "xref" table)
- Merge/survivorship: `POST /api/parties/{survivorId}/merge/{duplicateId}`
  re-points the duplicate's cross-references onto the survivor and marks the
  duplicate `MERGED`

This is intentionally a small, understandable slice of what a full MDM hub
does (match/merge, golden-record survivorship, source traceability) rather
than a clone of any specific 450-service enterprise product — a good base to
extend as you go.

## Running locally

1. Start Postgres:
   ```bash
   docker compose up -d postgres
   ```
2. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   (or `mvn spring-boot:run` if you don't want to use the wrapper)
3. Open the API docs: http://localhost:8080/swagger-ui.html
4. Health check: http://localhost:8080/actuator/health

Default connection settings (overridable via env vars `DB_HOST`, `DB_PORT`,
`DB_NAME`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`) match the Compose file:
db `mdmhub`, user/password `mdmhub`, port `5432`.

### Running everything in Docker

```bash
docker compose up --build
```
This builds the app image (multi-stage `Dockerfile`, JDK 21) and starts it
alongside Postgres, exposing the app on port 8080.

### Running tests

```bash
./mvnw test
```
Tests run against an in-memory H2 database (`application-test.yml`), so you
don't need Postgres running just to build.

## Example flow

```bash
# Register a source system
curl -X POST localhost:8080/api/source-systems \
  -H 'Content-Type: application/json' \
  -d '{"code":"CRM","name":"CRM System","description":"Customer records"}'

# Create a golden record
curl -X POST localhost:8080/api/parties \
  -H 'Content-Type: application/json' \
  -d '{"partyType":"INDIVIDUAL","firstName":"Vikram","lastName":"Shanbogar","email":"vikram@example.com"}'

# Link it to a CRM record
curl -X POST localhost:8080/api/parties/{partyId}/cross-references \
  -H 'Content-Type: application/json' \
  -d '{"sourceSystemCode":"CRM","sourceRecordId":"CRM-1001"}'

# Look a party up by its source system record
curl "localhost:8080/api/parties/lookup?sourceSystem=CRM&sourceRecordId=CRM-1001"

# Merge a duplicate into a survivor
curl -X POST localhost:8080/api/parties/{survivorId}/merge/{duplicateId}
```

## Kubernetes (learning setup, not production-hardened)

`k8s/postgres.yaml` and `k8s/app.yaml` give you a starting point:

```bash
kubectl apply -f k8s/postgres.yaml
docker build -t <your-registry>/mdm-hub:0.1.0 .
docker push <your-registry>/mdm-hub:0.1.0   # edit k8s/app.yaml with your image first
kubectl apply -f k8s/app.yaml
```

Notes: Postgres uses `emptyDir` (data is lost on pod restart — swap in a
`PersistentVolumeClaim` for anything durable), and the app's readiness/
liveness probes rely on Spring Boot's Kubernetes health-probe groups, which
are already enabled in `application.yml`.

## Where to take this next

Ideas if you want to keep extending it as a revision exercise:
- Fuzzy match/candidate-scoring before merge, instead of merging by id
- Outbox pattern + Kafka to publish golden-record change events (ties into
  the CDC/Kafka work you've done before)
- Push the Docker image to ECR and run this on EKS instead of local k8s
- Flyway/Liquibase migrations instead of `ddl-auto: update`
- Spring Security + method-level auth on the merge endpoint
