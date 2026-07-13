# Changelog

Todas las modificaciones notables de este proyecto se documentarán en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
