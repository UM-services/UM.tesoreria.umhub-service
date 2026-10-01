# AGENTS.md — um.tesoreria.umhub-service

Guía para agentes de IA que trabajan en este repositorio.

## Qué es este proyecto

Microservicio **concentrador (hub)** del sistema de tesorería de la Universidad de Mendoza. **No tiene base de datos**: expone una API REST protegida por API Key y delega toda la persistencia/lógica de negocio en otros microservicios del ecosistema mediante **OpenFeign**, además de consumir eventos de pago asincrónicos con **Kafka** y notificar al webhook de **n8n**.

Sigue **Arquitectura Hexagonal** (puertos y adaptadores) con dos slices (bounded contexts): `campanha` y `reservavacante`.

## Stack

- **Java 25** (no usar APIs anteriores si existe alternativa moderna) + **Lombok**
- **Spring Boot 4.1.x** (parent `spring-boot-starter-parent`; Jackson es `tools.jackson.*`, NO `com.fasterxml.jackson.databind` en configuración nueva — ver nota en Kafka)
- **Spring Cloud 2025.1.3**: Consul discovery, OpenFeign (+ feign-hc5)
- **Kafka** (`spring-boot-starter-kafka`), **Caffeine cache**, **SpringDoc OpenAPI**, **Actuator**
- **Maven** con wrapper (`./mvnw`); **JaCoCo** + **SonarCloud** (organización `um-services`)
- Empaquetado Docker multi-etapa (JRE 25 Alpine, usuario no privilegiado)

## Comandos

```bash
./mvnw clean package          # compilar + tests + JAR (target/um.tesoreria.umhub-service.jar)
./mvnw spring-boot:run        # ejecutar localmente (requiere Consul/Kafka o ajustar bootstrap.yml)
./mvnw test                   # solo tests
./mvnw -B verify              # como lo hace CI (antes de Sonar)
```

Requisitos locales: JDK 25 y Maven vía wrapper. Consul (`consul-service:8500` por defecto) y Kafka (`kafka:9092` por defecto) se esperan en docker-compose del ecosistema.

## Estructura y arquitectura hexagonal

Paquete raíz: `tesoreria.umhub`. Slices en `tesoreria.umhub.hexagonal.umhub.{slice}`:

```
{slice}/                          (ej. campanha, reservavacante)
├── domain/
│   ├── model/{Entity}.java        POJO Lombok, sin framework (solo java.* + Lombok)
│   └── ports/
│       ├── in/{Action}{Entity}UseCase.java   un interface por caso de uso
│       └── out/{Entity}ExternalService.java  puerto de salida → otro microservicio
│                                   (también {Entity}NotificationService p. ej. n8n)
├── application/
│   ├── usecases/{Action}{Entity}UseCaseImpl.java  @Component, delega al puerto out
│   └── service/{Entity}Service.java               @Service facade, inyecta UseCase(s)
└── infrastructure/
    ├── client/                     adaptador Feign: {Entity}FeignClientAdapter @Component
    │                               implementa el puerto out; {Entity}FeignClient @FeignClient;
    │                               DTOs Backend{Entity}Request/Response del contrato externo
    ├── web/
    │   ├── controller/{Entity}Controller.java   @RestController, solo DTOs
    │   ├── dto/{Entity}Request.java, {Entity}WrappedResponse.java
    │   └── mapper/{Entity}DtoMapper.java        Request↔dominio↔Response
    └── kafka/                      (reservavacante) PaymentProcessedConsumer + PaymentProcessedEvent
```

**Diferencia clave vs. la skill `hexagonal-arch` genérica**: este servicio NO tiene `infrastructure/persistence/` (no hay JPA, ni `Auditable`, ni `{Entity}Entity`). Los puertos de salida se implementan con adaptadores Feign en `infrastructure/client/` (y Kafka para inbound asíncrono). Aplicar igualmente las reglas estrictas de la skill:

- Dependencias apuntan hacia adentro: `infrastructure → application → domain`. `domain/` es 100% puro (solo `java.*` + Lombok). `application/` importa solo `domain/` + anotaciones Spring/Lombok.
- Nunca filtrar tipos de infraestructura (DTOs `Backend*`, tipos Feign) hacia `application/` o `domain/`.
- Los controllers hablan solo DTOs web (`{Entity}Request` / `{Entity}WrappedResponse`), nunca el modelo de dominio directamente en el body.
- No puentes de compatibilidad ni sobrecargas "legacy". Si endurecer un slice rompe consumidores, dejarlos fallar y reportarlos.
- **Dependencias cross-slice**: si un slice necesita tipos de otro slice, NO decidir unilateralmente — detenerse y preguntar al usuario (ver skill `hexagonal-arch`).

## Convenciones de código

- Inyección por constructor con `@RequiredArgsConstructor` (Lombok) en todos los componentes Spring.
- Modelos de dominio y DTOs: Lombok `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` (o `@Data` en DTOs anidados).
- IDs: `UUID` (provienen del backend `tesoreria-core-service`).
- **Dominio/nombre comercial en español** (`nombre`, `valorReserva`, `creadoEn`, `mensaje`, `estado`); logs y nombres técnicos en inglés. Comentarios existentes en ambos idiomas — mantener coherencia con el archivo.
- Logs con `@Slf4j`.
- Mappers: método por conversión (`toDomain`, `toWrappedResponse`, etc.), `null` de entrada → `null` de salida; respetar la regla de propagación de `@Builder.Default` de la skill.

## API y seguridad

- Rutas públicas del hub: `/api/tesoreria/umhub/{campanha|reservaVacante}/...` (ver tabla completa en README.md).
- Rutas del backend vía Feign: `/api/tesoreria/core/umhub/...` hacia el servicio `tesoreria-core-service` (resuelto por Consul). `@FeignClient` siempre con `contextId` único cuando hay varios clients hacia el mismo servicio.
- **Autenticación**: header `X-API-Key` validado por `ApiKeyFilter` (`app.api-key`). Exentos: `/actuator`, `/swagger-ui`, `/v3/api-docs`. Cualquier endpoint nuevo queda protegido automáticamente.
- Formato de respuesta: `{ "success": bool, "data": {...}, "mensaje": "..." }` (`{Entity}WrappedResponse`); campos JSON del contrato externo/respuestas en **snake_case** vía `@JsonProperty`.
- Swagger UI con seguridad `X-API-Key` declarada en `OpenApiConfig`.

## Kafka / n8n

- Topic consumido: `payment-processed`, groupId `tesoreria-umhub-group` (override por `SPRING_KAFKA_*`).
- `KafkaConsumerConfig`: deserializador JSON con `ErrorHandlingDeserializer`, `FAIL_ON_UNKNOWN_PROPERTIES` deshabilitado, trusted packages `*`, mapeo de tipo header `um.tesoreria.mercadopago.service.domain.event.PaymentProcessedEvent` → `PaymentProcessedEvent` local. Nota: usa `tools.jackson` (Jackson 3, Spring Boot 4).
- El consumer filtra: ignora eventos con `reservaVacanteId == null` y estados != `approved`; los errores se loguean y se tragán (retry fijo 1s x2 por `DefaultErrorHandler`) — **no propagar excepciones que maten el listener**.
- Notificación a n8n: Feign client con `url = "${app.n8n.webhook-url}"` hacia `POST /webhook/mercado-pago`.

## Configuración

- Único archivo: `src/main/resources/bootstrap.yml` (no crear `application.yml`).
- Variables de entorno: `APP_PORT`, `APP_API_KEY`, `APP_N8N_WEBHOOK_URL`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SPRING_KAFKA_CONSUMER_GROUP_ID`; Consul en `consul-service:8500`.
- `@EnableFeignClients(basePackages = "tesoreria.umhub")` en `UmHubConfiguration`.

## Tests

- Cobertura actual mínima: `UmHubApplicationTests` (contextLoads, `@SpringBootTest` con `spring.kafka.listener.auto-startup=false` y bootstrap-servers fake — **mantener estas properties o el contexto no levanta sin Kafka**) y `HelloTest` (controller smoke-test en `src/main`, no es un JUnit test).
- Tests nuevos: JUnit 5 + Spring Boot Test, unitarios para use cases/mappers (Mockito viene en `spring-boot-starter-test`); ubicar en `src/test/java/tesoreria/umhub/...` espejando el slice. SonarCloud mide cobertura con JaCoCo (`target/site/jacoco/jacoco.xml`).

## Documentación y versionado (obligatorio en cada feature)

1. **README.md**: badges de versión, tabla de endpoints, tabla de diagramas.
2. **CHANGELOG.md**: formato Keep a Changelog; subir versión **SemVer** en `pom.xml` (`<version>`) y en el badge del README en cada release.
3. **docs/diagrams/*.mmd**: Mermaid (arquitectura general, flujos hexagonales, secuencias). Si se agrega/cambia un slice o flujo, actualizar/crear el `.mmd` correspondiente; el pipeline `generate-docs.yml` los inyecta en `docs/index.html` (GitHub Pages) — si se crea un diagrama nuevo, también se debe agregar su bloque de inyección al workflow.

## Git

- Ramas: `main` (released) y `develop`; ramas de trabajo nombradas `{número-issue}-{descripción}` (kebab-case, a veces con prefijo `feat`/`fix`).
- Commits: **Conventional Commits** en inglés, con scope de slice cuando aplica: `feat(reservavacante): add Kafka payment processing and n8n notification`.
- Cambios llegan a `main` vía PR (merge commits visibles en historia); CI en PR: `maven.yml` (build + Sonar + push imagen Docker a Docker Hub con sufijo/etiquetas semver) y `generate-docs.yml`.

## Reglas finales para el agente

- Al crear un slice nuevo, seguir la estructura de `campanha`/`reservavacante` y las convenciones de nombres de la skill `hexagonal-arch` (adaptada: puertos out = servicios externos, no repos JPA).
- Verificar que el proyecto compile con `./mvnw -q compile` (y `./mvnw test` si hay tests) antes de dar por terminada una tarea.
- No introducir dependencias de base de datos ni Spring Security (la seguridad es el filtro de API Key propio).
- Si una regla de este archivo entra en conflicto con la skill `hexagonal-arch`, prevalece la estructura real de este repo (sin persistencia) para `infrastructure/`; el resto de las reglas estrictas se mantiene.
