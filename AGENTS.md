# AGENTS.md — um.tesoreria.umhub-service

Guide for AI agents working in this repository.

## What this project is

**Concentrator (hub)** microservice of the Universidad de Mendoza treasury system. **It has no database**: it exposes a REST API protected by API Key and delegates all persistence/business logic to other microservices in the ecosystem via **OpenFeign**, in addition to consuming asynchronous payment events with **Kafka** and notifying the **n8n** webhook.

It follows **Hexagonal Architecture** (ports and adapters) with two slices (bounded contexts): `campanha` and `reservavacante`.

## Stack

- **Java 25** (do not use older APIs when a modern alternative exists) + **Lombok**
- **Spring Boot 4.1.x** (parent `spring-boot-starter-parent`; Jackson is `tools.jackson.*`, NOT `com.fasterxml.jackson.databind` in new configuration — see the note under Kafka)
- **Spring Cloud 2025.1.4**: Consul discovery, OpenFeign (+ feign-hc5)
- **Kafka** (`spring-boot-starter-kafka`), **Caffeine cache**, **SpringDoc OpenAPI**, **Actuator**
- **Maven** with wrapper (`./mvnw`); **JaCoCo** + **SonarCloud** (organization `um-services`)
- Multi-stage Docker packaging (JRE 25 Alpine, non-root user)

## Commands

```bash
./mvnw clean package          # build + tests + JAR (target/um.tesoreria.umhub-service.jar)
./mvnw spring-boot:run        # run locally (requires Consul/Kafka or adjust bootstrap.yml)
./mvnw test                   # tests only
./mvnw -B verify              # as CI does it (before Sonar)
```

Local requirements: JDK 25 and Maven via wrapper. Consul (`consul-service:8500` by default) and Kafka (`kafka:9092` by default) are expected in the ecosystem docker-compose.

## Structure and hexagonal architecture

Root package: `tesoreria.umhub`. Slices in `tesoreria.umhub.hexagonal.umhub.{slice}`:

```
{slice}/                          (e.g. campanha, reservavacante)
├── domain/
│   ├── model/{Entity}.java        Lombok POJO, no framework (only java.* + Lombok)
│   └── ports/
│       ├── in/{Action}{Entity}UseCase.java   one interface per use case
│       └── out/{Entity}ExternalService.java  outbound port → another microservice
│                                   (also {Entity}NotificationService, e.g. n8n)
├── application/
│   ├── usecases/{Action}{Entity}UseCaseImpl.java  @Component, delegates to the outbound port
│   └── service/{Entity}Service.java               @Service facade, injects UseCase(s)
└── infrastructure/
    ├── client/                     Feign adapter: {Entity}FeignClientAdapter @Component
    │                               implements the outbound port; {Entity}FeignClient @FeignClient;
    │                               DTOs Backend{Entity}Request/Response from the external contract
    ├── web/
    │   ├── controller/{Entity}Controller.java   @RestController, DTOs only
    │   ├── dto/{Entity}Request.java, {Entity}WrappedResponse.java
    │   └── mapper/{Entity}DtoMapper.java        Request↔domain↔Response
    └── kafka/                      (reservavacante) PaymentProcessedConsumer + PaymentProcessedEvent
```

**Key difference vs. the generic `hexagonal-arch` skill**: this service has NO `infrastructure/persistence/` (no JPA, no `Auditable`, no `{Entity}Entity`). Outbound ports are implemented with Feign adapters in `infrastructure/client/` (and Kafka for asynchronous inbound). Still apply the skill's strict rules:

- Dependencies point inward: `infrastructure → application → domain`. `domain/` is 100% pure (only `java.*` + Lombok). `application/` imports only `domain/` + Spring/Lombok annotations.
- Never leak infrastructure types (DTOs `Backend*`, Feign types) into `application/` or `domain/`.
- Controllers speak only web DTOs (`{Entity}Request` / `{Entity}WrappedResponse`), never the domain model directly in the body.
- No compatibility bridges or "legacy" overloads. If making a slice strict breaks consumers, leave them failing and report it.
- **Cross-slice dependencies**: if a slice needs types from another slice, do NOT decide unilaterally — stop and ask the user (see the `hexagonal-arch` skill).

## Code conventions

- Constructor injection with `@RequiredArgsConstructor` (Lombok) in all Spring components.
- Domain models and DTOs: Lombok `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` (or `@Data` on nested DTOs).
- IDs: `UUID` (they come from the `tesoreria-core-service` backend).
- **Domain/business names in Spanish** (`nombre`, `valorReserva`, `creadoEn`, `mensaje`, `estado`); logs and technical names in English. Existing comments are in both languages — stay consistent with the file.
- Logs with `@Slf4j`.
- Mappers: one method per conversion (`toDomain`, `toWrappedResponse`, etc.), `null` in → `null` out; respect the skill's `@Builder.Default` propagation rule.

## API and security

- Public hub routes: `/api/tesoreria/umhub/{campanha|reservaVacante}/...` (see the full table in README.md).
- Backend routes via Feign: `/api/tesoreria/core/umhub/...` to the `tesoreria-core-service` service (resolved by Consul). `@FeignClient` always with a unique `contextId` when there are several clients to the same service.
- **Authentication**: header `X-API-Key` validated by `ApiKeyFilter` (`app.api-key`). Exempt: `/actuator`, `/swagger-ui`, `/v3/api-docs`. Any new endpoint is automatically protected.
- Response format: `{ "success": bool, "data": {...}, "mensaje": "..." }` (`{Entity}WrappedResponse`); external-contract/response JSON fields in **snake_case** via `@JsonProperty`.
- Swagger UI with `X-API-Key` security declared in `OpenApiConfig`.

## Kafka / n8n

- Consumed topic: `payment-processed`, groupId `tesoreria-umhub-group` (override with `SPRING_KAFKA_*`); dead-letter topic: `payment-processed.DLT`.
- `KafkaConsumerConfig`: JSON deserializer pinned to the target type (`JacksonJsonDeserializer<>(PaymentProcessedEvent.class, objectMapper, false)`), `ErrorHandlingDeserializer`, `FAIL_ON_UNKNOWN_PROPERTIES` disabled; `__TypeId__` is ignored (no type mapper, no `trusted.packages`). Note: it uses `tools.jackson` (Jackson 3, Spring Boot 4). Retries use `ExponentialBackOff` (1 s × 2, 5 attempts) and `DeadLetterPublishingRecoverer` (JSON template for deserialized events, `byte[]` template for records that failed deserialization).
- `PaymentProcessedConsumer`: filters events with `reservaVacanteId == null` and states != `approved`; it claims the notification in core (`POST .../reservaVacante/{id}/notificacion/claim`) before notifying and becomes a no-op if another delivery already notified. On failure it releases the claim (`.../notificacion/release`) and rethrows so Kafka retries and eventually routes the record to the DLT — **do not swallow exceptions**.
- `KafkaProducerConfig`: `KafkaTemplate<String, Object>` (JSON) and `KafkaTemplate<String, byte[]>` (binary) used only to publish to `payment-processed.DLT` (the service produces no business events).
- n8n notification: Feign client with `url = "${app.n8n.webhook-url}"` to `POST /webhook/mercado-pago`; `ReservaVacanteNotificationFeignAdapter.notifyPayment` must not catch the error, otherwise the claim is never released.

## Configuration

- Single file: `src/main/resources/bootstrap.yml` (do not create `application.yml`).
- Environment variables: `APP_PORT`, `APP_API_KEY`, `APP_N8N_WEBHOOK_URL`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SPRING_KAFKA_CONSUMER_GROUP_ID`, `SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET` (default `latest`); Consul at `consul-service:8500`.
- Consul discovery: `query-passing: true` and health check on `/actuator/health/readiness` (`health-check-critical-timeout: 5m`) so Feign/LoadBalancer only resolve instances whose checks pass.
- `@EnableFeignClients(basePackages = "tesoreria.umhub")` in `UmHubConfiguration`.

## Tests

- Current minimum coverage: `UmHubApplicationTests` (contextLoads, `@SpringBootTest` with `spring.kafka.listener.auto-startup=false` and a fake bootstrap-servers — **keep these properties or the context will not start without Kafka**) and `HelloTest` (controller smoke test in `src/main`, not a JUnit test).
- New tests: JUnit 5 + Spring Boot Test, unit tests for use cases/mappers (Mockito comes with `spring-boot-starter-test`); place them in `src/test/java/tesoreria/umhub/...` mirroring the slice. SonarCloud measures coverage with JaCoCo (`target/site/jacoco/jacoco.xml`).

## Documentation and versioning (mandatory on every feature)

1. **README.md**: version badges, endpoint table, diagram table.
2. **CHANGELOG.md**: Keep a Changelog format; bump the **SemVer** version in `pom.xml` (`<version>`) and in the README badge on each release.
3. **docs/diagrams/*.mmd**: Mermaid (general architecture, hexagonal flows, sequences). If a slice or flow is added/changed, update/create the corresponding `.mmd`; the `generate-docs.yml` pipeline injects them into `docs/index.html` (GitHub Pages) — if a new diagram is created, its injection block must also be added to the workflow.

## Git

- Branches: `main` (released) and `develop`; working branches named `{issue-number}-{description}` (kebab-case, sometimes with a `feat`/`fix` prefix).
- Commits: **Conventional Commits** in English, with a slice scope when applicable: `feat(reservavacante): add Kafka payment processing and n8n notification`.
- Changes reach `main` via PR (merge commits visible in history); CI on PR: `maven.yml` (build + Sonar + push Docker image to Docker Hub with suffix/semver tags) and `generate-docs.yml`.

## Final rules for the agent

- When creating a new slice, follow the structure of `campanha`/`reservavacante` and the naming conventions of the `hexagonal-arch` skill (adapted: outbound ports = external services, not JPA repositories).
- Verify the project compiles with `./mvnw -q compile` (and `./mvnw test` if there are tests) before considering a task done.
- Do not introduce database dependencies or Spring Security (security is the service's own API Key filter).
- If a rule in this file conflicts with the `hexagonal-arch` skill, the real structure of this repo (no persistence) prevails for `infrastructure/`; the rest of the strict rules still apply.

## Permissions (feature gating)

The permission system is **centralized** and lives in `tesoreria-core-service` (catalog `permiso`, roles, role×permission matrix, overrides and the effective bundle `GET /api/tesoreria/core/permisoEfectivo/usuario/{userId}`). Keys follow the `modulo.accion` convention.

- If you add a new feature that requires a permission: register the key in the catalog (from the administrador module) and wire it in the **new** code. Server-side enforcement (`@RequierePermiso`) lives in core; this service has **none**.
- **Golden rule:** never add or change security on existing endpoints or legacy code. The legacy system sends no token or permissions: any enforcement there breaks it. When in doubt, stop and ask.
