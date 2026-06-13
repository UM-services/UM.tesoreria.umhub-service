# Changelog

Todas las modificaciones notables de este proyecto se documentarán en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
