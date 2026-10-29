# Changelog

Todas las modificaciones notables de este proyecto se documentarán en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.9.1] - 2026-10-29

### Added

- Variable de entorno `SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET` (default `latest`) expuesta en `bootstrap.yml` como `spring.kafka.consumer.auto-offset-reset` y aplicada en `KafkaConsumerConfig` al `ConsumerFactory`.
- `spring.kafka.admin.fail-fast: true` y `spring.kafka.admin.modify-topic-configs: true` en `bootstrap.yml`.
- `KafkaProducerConfigTest`: verifica que el payload del DLT se serializa sin el header `__TypeId__` y que el `ProducerFactory` usa el bean `JacksonJsonSerializer` con `addTypeInfo=false`.

### Changed

- `KafkaConsumerConfig` ahora pasa `ConsumerConfig.AUTO_OFFSET_RESET_CONFIG` al cliente con default `latest`: un grupo de consumers nuevo ya no reproduce ni re-notifica pagos viejos de `payment-processed`. Se eliminan las propiedades de clase de deserializador key/value, que no tenían efecto porque las instancias se entregan directamente al `DefaultKafkaConsumerFactory`.
- `KafkaProducerConfig` declara un bean `JacksonJsonSerializer<Object>` con `addTypeInfo=false` y lo inyecta explícitamente en `DefaultKafkaProducerFactory` y en `KafkaTemplate`: el registro del DLT queda con el JSON del evento y sin `__TypeId__`, un header que apuntaba a una clase inexistente en los demás servicios.
- `UmHubApplicationTests` excluye `KafkaAutoConfiguration` en el perfil de test (además de `spring.kafka.listener.auto-startup=false`), para que el contexto no espere la creación de topics sin broker.
- Con `spring.kafka.admin.fail-fast: true`, el arranque falla de forma explícita si `KafkaAdmin` no puede conectarse o administrar topics; el despliegue requiere Kafka disponible antes de levantar el servicio.

Sin cambios de API, endpoints, contrato HTTP ni dependencias.

## [0.9.0] - 2026-10-23

### Added

- `ReservaVacanteNotificacionClaimPort` + `ReservaVacanteNotificacionClaimFeignClient`/`...ClaimFeignAdapter`: consumen los endpoints internos `POST /api/tesoreria/core/umhub/reservaVacante/{id}/notificacion/claim|release` de core, que son el claim atómico de la notificación de pago. umhub no tiene base de datos, así que la marca de "ya notificado" vive en core (`reserva_vacante.notificacion_pago`).
- `KafkaProducerConfig` con `KafkaTemplate<String, Object>` (JSON) y `KafkaTemplate<String, byte[]>` (binario) para publicar en `payment-processed.DLT`.
- Nuevos campos en `PaymentProcessedEvent`: `dateLastUpdated` (`date_last_updated` del pago en MercadoPago, orden de los eventos) y `source` (`WEBHOOK_V1` | `IPN_LEGACY` | `MANUAL`).
- `PaymentProcessedConsumerTest`: claim perdido ⇒ no notifica; envío fallido ⇒ `release` + excepción propagada; liberación fallida ⇒ excepción suprimida; reserva inexistente ⇒ `release` + excepción propagada (deriva al DLT).
- `KafkaConsumerConfigTest`: deserializa `payment-processed` sin headers, con el `__TypeId__` legacy del productor anterior y con un type id desconocido.

### Changed

- **Requiere `tesoreria-core-service` con los endpoints de claim** (`reservaVacante/{id}/notificacion/claim|release`) desplegado antes o junto con este release: sin ellos el consumer no puede reclamar la notificación y los pagos aprobados terminan en `payment-processed.DLT`.
- Consul: `query-passing: true` (Feign/LoadBalancer sólo resuelven instancias con los checks en passing) y health check registrado contra `/actuator/health/readiness` con poda del servicio a los 5 minutos en estado crítico.
- `PaymentProcessedConsumer` es idempotente: pide el claim de la reserva antes de notificar, si otra entrega del evento ya notificó termina en no-op, y si el envío falla libera el claim y propaga la excepción para que Kafka reintente. Ya no se traga los errores (antes el offset avanzaba y la notificación se perdía). (`containerFactory` explícito para que aplique el DLT.)
- `ReservaVacanteNotificationFeignAdapter.notifyPayment` deja de capturar la excepción del POST a n8n: el consumer necesita enterarse para liberar el claim y reintentar.
- `KafkaConsumerConfig` deserializa por **tipo target** (`useHeadersIfPresent=false`) e ignora `__TypeId__`; se eliminan el mapping FQCN `um.tesoreria.mercadopago.service.domain.event.PaymentProcessedEvent` y `trusted.packages("*")`.
- `KafkaConsumerConfig`: reintentos con backoff exponencial (1 s × 2, 5 reintentos) y derivación a `payment-processed.DLT` con `DeadLetterPublishingRecoverer`, en lugar de `FixedBackOff(1000, 2)` sin recoverer (el record se descartaba).
- Spring Boot (parent) actualizado de `4.1.1` a `4.1.2`.
- Spring Cloud actualizado de `2025.1.3` a `2025.1.4`.
- `springdoc-openapi-starter-webmvc-ui` actualizado de `3.1.0` a `3.1.1`.
- `jacoco-maven-plugin` actualizado de `0.8.13` a `0.8.15`.

## [0.8.1] - 2026-10-19

### Changed

- `AGENTS.md` traducido íntegramente al inglés y ampliado con la sección **Permissions (feature gating)**: el sistema de permisos es centralizado en `tesoreria-core-service` (catálogo `permiso`, roles, matriz rol×permiso, overrides y bundle efectivo `GET /api/tesoreria/core/permisoEfectivo/usuario/{userId}`), con claves en el formato `modulo.accion`; se documenta además la regla de no agregar ni modificar seguridad en endpoints existentes ni en código legacy, porque el sistema legacy no envía token ni permisos.
- `.github/workflows/deploy-staging.yml`: el job `deploy` ahora se ejecuta con una matriz `staging` / `staging-new` (`fail-fast: false`), selecciona el runner self-hosted por entorno (`runs-on: [self-hosted, linux, "${{ matrix.environment }}"]`) y usa el GitHub Environment correspondiente. El job `build-image` y el flujo de verificación no cambian; no hay cambios en la aplicación ni en su API.

### Fixed

- Cerrado el bloque de código Mermaid (fence de apertura y cierre) en `docs/diagrams/flujo-campanha.mmd`, `flujo-reservavacante.mmd`, `flujo-creacion-campanha.mmd` y `flujo-consulta-reservavacante.mmd`. Al faltar la fence final, el paso `sed '1d;$d'` de `generate-docs.yml` eliminaba la última línea real del diagrama (por ejemplo `MAP --> RES` o el `end` de la secuencia), produciendo diagramas incompletos o con error de sintaxis en GitHub Pages. Los siete diagramas vuelven a renderizar correctamente con Mermaid CLI.

## [0.8.0] - 2026-10-04

### Added

- Nuevo slice hexagonal `consulta` (bridge de solo lectura hacia `tesoreria-core-service`): endpoints `GET /api/tesoreria/umhub/persona/{numeroDocumento}` (identidad, tipos de documento, contacto —emails, teléfono, celular— y domicilio con provincia/localidad resueltas) y `GET /api/tesoreria/umhub/persona/{numeroDocumento}/deuda` (deuda agregada por chequera; `?extended=true` agrega vencimientos con `init_point` de MercadoPago). La consulta usa **sólo el número de documento** (6-10 dígitos, validado en el hub); el tipo se resuelve en core agregando todos los tipos del mismo titular. Respuestas envueltas `{ success, data, mensaje }` en `snake_case`, protegidas automáticamente por `ApiKeyFilter` (`X-API-Key`).
- `ConsultaFeignClient` (`contextId = "consultaClient"`) con configuración propia `ConsultaFeignClientConfig`: timeouts 5 s connect / 15 s read (no hereda los 200 s globales). El 404 del backend se traduce a `404` envuelto `{success:false}`; otros fallos Feign a `502` con mensaje genérico.
- Caché Caffeine en `ConsultaFeignClientAdapter` (TTL 60 s, máximo 500 entradas por tipo de consulta, sólo resultados positivos): amortigua consultas repetidas y el costo de recálculo de deuda.
- `RateLimitFilter` (una API Key única, ventana fija de 1 minuto por cliente —`X-Forwarded-For` o IP—) limitado a `/api/tesoreria/umhub/persona`: `429` con `Retry-After` al exceder `app.rate-limit.requests-per-minute` (default 60). Configurables vía `APP_RATE_LIMIT_PER_MINUTE` y `APP_RATE_LIMIT_ENABLED`. Mitiga enumeración de documentos sobre endpoints que exponen PII de contacto.
- Diagrama `docs/diagrams/flujo-consulta-persona-deuda.mmd` y su inyección en el pipeline `generate-docs.yml`.
- `docs/diagrams/arquitectura-general.mmd` actualizado: slice `consulta` (controller → service → use cases → ports → adaptador → Feign → core) y `RateLimitFilter` en la cadena de filtros con su rama `429`.
- Pruebas: `ConsultaFeignClientAdapterTest` (mapeo, 404→empty, caché positivo/negativo, propagación de errores), `ConsultaDtoMapperTest` (incluye verificación de claves `snake_case` en el JSON), `ConsultaPersonaControllerTest` (contrato, 404/400/502, `extended`) y `RateLimitFilterTest`.
- Dependencia de test `spring-boot-starter-webmvc-test` (necesaria para `@WebMvcTest` en Spring Boot 4).

### Changed

- `ApiKeyFilter` ahora se anota con `@Order(1)` para garantizar que se evalúa antes de `RateLimitFilter` (`@Order(2)`).

## [0.7.1] - 2026-10-01

### Added

- Nuevos pipelines de despliegue `.github/workflows/deploy-develop.yml` y `.github/workflows/deploy-staging.yml`: verificación con `mvn -B verify` (JDK 25), build y push de la imagen Docker etiquetada con el SHA del commit, y despliegue en los entornos `develop` y `staging` mediante runners self-hosted con GitHub Environments.
- Guía `AGENTS.md` para agentes de IA con la estructura hexagonal sin persistencia, stack, comandos y convenciones del proyecto.

### Changed

- Spring Boot (parent) actualizado de `4.1.0` a `4.1.1`.
- Spring Cloud actualizado de `2025.1.2` a `2025.1.3`.
- `springdoc-openapi-starter-webmvc-ui` actualizado de `3.0.3` a `3.1.0`.
- Dependencia de Kafka migrada de `spring-kafka` a `spring-boot-starter-kafka` (versionada por el parent de Spring Boot; sin cambio de comportamiento).
- Banner de arranque (`src/main/resources/banner.txt`) renovado con nuevo arte ASCII.

## [0.7.0] - 2026-07-13

### Added

- Nuevo consumer Kafka `PaymentProcessedConsumer` que escucha eventos `payment-processed` en el topic de Kafka, procesando notificaciones de pago aprobado para reservas de vacante.
- Nuevo puerto de dominio `ReservaVacanteNotificationService` para notificaciones de pago.
- Nuevo adaptador Feign `ReservaVacanteNotificationFeignAdapter` que envía notificaciones de pago al webhook de n8n (`/webhook/mercado-pago`).
- Nuevo cliente Feign `ReservaVacanteNotificationFeignClient` para integración con n8n webhook.
- Nuevo DTO `PaymentProcessedEvent` que modela los eventos de pago procesado recibidos desde Kafka.
- Nuevo DTO `N8nNotificationPayload` con modelo de payload para notificaciones a n8n, incluyendo `PagoDetails` para información detallada del pago.
- Configuración del consumer Kafka (`KafkaConsumerConfig`) con deserialización JSON, manejo de errores y paquete de confianza para `PaymentProcessedEvent`.
- Dependencia `spring-kafka` agregada al proyecto.
- Configuración de Kafka (`spring.kafka.bootstrap-servers`, `spring.kafka.consumer.group-id`) en `bootstrap.yml`.
- Configuración de n8n webhook URL (`app.n8n.webhook-url`) en `bootstrap.yml`.
- Plugin JaCoCo (`jacoco-maven-plugin` 0.8.13) para generación de reportes de cobertura de código.
- Propiedades de SonarCloud (`sonar.organization`, `sonar.projectKey`, `sonar.host.url`, `sonar.coverage.jacoco.xmlReportPaths`) en `pom.xml`.

### Changed

- Test `UmHubApplicationTests` actualizado para desactivar auto-startup de Kafka y configurar bootstrap-servers de prueba.
- Corregido `artifactId` en `pom.xml` (`esoreria-umhub-service` → `umhub-service`).

## [0.6.0] - 2026-06-19

### Added

- Nuevo campo `importe` (BigDecimal) en el modelo de dominio `ReservaVacante`, propagado a través de DTOs (`BackendReservaVacanteRequest`, `BackendReservaVacanteResponse`, `ReservaVacanteRequest`, `ReservaVacanteWrappedResponse`), mapper (`ReservaVacanteDtoMapper`) y adaptador Feign (`ReservaVacanteFeignClientAdapter`), tanto en el flujo de creación como en el de consulta de estado.

## [0.5.0] - 2026-06-15

### Added

- Nuevo campo `initPoint` (String) en el modelo de dominio `ReservaVacante`, propagado a través de DTOs (`BackendReservaVacanteResponse`, `ReservaVacanteWrappedResponse`), mapper y adaptador Feign.

## [0.4.0] - 2026-06-15

### Added

- Nuevo campo `valorReserva` (BigDecimal) en el modelo de dominio `Campanha`, propagado a través de DTOs de solicitud/respuesta, mapper y adaptador Feign.
- Nuevo endpoint REST `GET /api/tesoreria/umhub/reservaVacante/reserva/status/{id}` para consultar el estado de una reserva de vacante.
- Nuevo caso de uso `GetReservaVacanteStatusUseCase` con su implementación en la capa de aplicación.
- Nuevo método `getStatus(UUID)` en el puerto de salida `ReservaVacanteExternalService` y su implementación en `ReservaVacanteFeignClientAdapter`.
- Nuevo método `getReservaVacanteStatus` en `ReservaVacanteFeignClient` (GET a `tesoreria-core-service`).
- Diagrama Mermaid `flujo-consulta-reservavacante.mmd` documentando el flujo de consulta de estado.
- Pipeline de documentación automática (`📄.github/workflows/docs.yml`) con generación de SVG desde Mermaid y despliegue a GitHub Pages.

### Changed

- Dependencia `springdoc-openapi-starter-webmvc-ui` actualizada de `3.0.2` a `3.0.3`.
- Diagrama `generacion-reporte.mmd` renombrado a `flujo-creacion-campanha.mmd` para reflejar correctamente su contenido (flujo de creación de campaña).
- Diagrama `flujo-reservavacante.mmd` actualizado para incluir el nuevo caso de uso `GetReservaVacanteStatusUseCase`.

## [0.3.0] - 2026-06-15

### Added

- Nuevo módulo `ReservaVacante` con arquitectura hexagonal: dominio (modelo, puertos), aplicación (servicio, caso de uso) e infraestructura (controlador REST, cliente Feign, adaptador, DTOs, mapper).
- Nuevo endpoint REST `POST /api/tesoreria/umhub/reservaVacante/vacante/add` para creación de reservas de vacante.
- Cliente Feign `ReservaVacanteFeignClient` con `contextId = "reservaVacanteClient"` para comunicación con `tesoreria-core-service`.
- Adaptador `ReservaVacanteFeignClientAdapter` que implementa el puerto de salida `ReservaVacanteExternalService`.
- Diagrama Mermaid `flujo-reservavacante.mmd` documentando el flujo hexagonal del nuevo módulo.

### Changed

- Rutas internas de FeignClient para campañas actualizadas de `/api/tesoreria/umhub/campanha/` a `/api/tesoreria/core/umhub/campanha/` para alinearse con el backend.
- `CampanhaFeignClient` ahora incluye `contextId = "campanhaClient"` para evitar conflictos con otros Feign Clients en el mismo `tesoreria-core-service`.
- Diagrama de arquitectura general actualizado para incluir el nuevo módulo `ReservaVacante`.

## [0.2.0] - 2026-06-13

### Added

- Arquitectura hexagonal para el módulo de campañas (dominio, aplicación e infraestructura).
- Nuevos endpoints REST para gestión de campañas: `POST /api/tesoreria/umhub/campanha/` y `PUT /api/tesoreria/umhub/campanha/update/{id}`.
- Cliente Feign `CampanhaFeignClient` para comunicación con `tesoreria-core-service`.
- Adaptador `CampanhaFeignClientAdapter` que implementa el puerto de salida `CampanhaExternalService`.
- Autenticación mediante API Key (`X-API-Key`) vía filtro `ApiKeyFilter`, con exclusión de rutas públicas (`/actuator`, `/swagger-ui`, `/v3/api-docs`).
- Configuración OpenAPI con `SecurityScheme` de tipo API Key.
- Dependencia explícita de Lombok en `pom.xml`.

### Changed

- Spring Boot actualizado de `4.0.7` a `4.1.0`.
- Spring Cloud actualizado de `2025.1.1` a `2025.1.2`.

## [0.1.0] - 2026-06-12

### Added

- Nuevo endpoint REST `/api/tesoreria/umhub/hello/test` como controlador de prueba del servicio.
- Badges tecnológicos en README.md.

### Fixed

- Corregido el nombre de la universidad en la descripción del proyecto (Montevideo → Mendoza).

## [0.0.1] - 2026-06-12

### Added

- Configuración inicial del proyecto Spring Boot 4.0.7 con Java 25.
- Integración con Spring Cloud Consul para descubrimiento y registro de servicios.
- Cliente HTTP declarativo mediante OpenFeign con Apache HC5.
- Documentación interactiva de API con SpringDoc OpenAPI 3.0.2.
- Caché en memoria con Caffeine.
- Monitoreo y métricas con Spring Boot Actuator.
- Pipeline de CI/CD con Maven, SonarCloud y Docker.
- Pipeline de documentación automática con Mermaid y GitHub Pages.
- Configuración multi-etapa Docker con JRE 25 Alpine y usuario no privilegiado.
- Prueba de contexto con Spring Boot Test.
