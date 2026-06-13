# UM Tesoreria Umhub Service

![Java 25](https://img.shields.io/badge/Java-25-%23ED8B00?logo=openjdk)
![Spring Boot 4.1.0](https://img.shields.io/badge/Spring_Boot-4.1.0-%236DB33F?logo=springboot)
![Spring Cloud 2025.1.2](https://img.shields.io/badge/Spring_Cloud-2025.1.2-%236DB33F?logo=spring)
![Maven](https://img.shields.io/badge/Maven-3-%23C71A36?logo=apachemaven)
![Docker](https://img.shields.io/badge/Docker-✓-%232496ED?logo=docker)
![Consul](https://img.shields.io/badge/Consul-Discovery-%23CA2171?logo=consul)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-%2361DAFB?logo=openapiinitiative)
![Version 0.2.0](https://img.shields.io/badge/Version-0.2.0-%23333?logo=semver)

Microservicio de concentrador (hub) para el sistema de tesorería de la Universidad de Mendoza. Actúa como punto de integración centralizado, exponiendo funcionalidades mediante una API REST (restringida por API Key) y siguiendo una arquitectura hexagonal con puertos y adaptadores. Utiliza OpenFeign para la comunicación con otros microservicios del ecosistema.

## Stack Tecnológico

- **Java 25**
- **Spring Boot 4.1.0**
- **Spring Cloud 2025.1.2**
- **Maven 3**
- **API Key Authentication** — seguridad mediante header `X-API-Key`
- **Arquitectura Hexagonal** — módulo de campañas con puertos y adaptadores
- **Consul Discovery** — registro y descubrimiento de servicios
- **OpenFeign + Feign HC5** — cliente HTTP declarativo con Apache HC5
- **SpringDoc OpenAPI 3.0.2** — documentación interactiva de la API
- **Caffeine Cache** — caché en memoria de alto rendimiento
- **Actuator** — métricas y monitoreo
- **Docker** — imagen multi-etapa con JRE 25 Alpine

## Requisitos

- JDK 25
- Maven 3
- Consul (o Docker para entornos locales)

## Ejecución local

```bash
# Compilar
./mvnw clean package

# Ejecutar
./mvnw spring-boot:run
```

## Docker

```bash
docker build -t umhub-service .
docker run -p 8080:8080 umhub-service
```

## API

### Campañas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/tesoreria/umhub/campanha/` | Crear una nueva campaña |
| `PUT` | `/api/tesoreria/umhub/campanha/update/{id}` | Actualizar una campaña existente |

Todos los endpoints requieren el header `X-API-Key` con la clave configurada.

## Documentación

La documentación interactiva del proyecto se despliega automáticamente en GitHub Pages y se actualiza en cada push a `main`. Incluye diagramas de arquitectura y secuencia generados con Mermaid.

## Versionado

Siguiendo [SemVer](https://semver.org/). Ver [CHANGELOG.md](CHANGELOG.md).
