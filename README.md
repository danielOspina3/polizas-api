# API de Gestión de Pólizas

[![CI](https://github.com/danielOspina3/polizas-api/actions/workflows/ci.yml/badge.svg)](https://github.com/danielOspina3/polizas-api/actions/workflows/ci.yml)

API REST para gestionar pólizas de arrendamiento **individuales** y **colectivas**: consultar pólizas y riesgos, renovar con IPC, cancelar pólizas y riesgos, y agregar riesgos a pólizas colectivas. Cada operación que modifica una póliza se notifica a un **mock del CORE** de seguros.

Prueba técnica práctica (Módulo 2) – Desarrollador/a Full Stack, Seguros Bolívar.
Autor: Daniel Mauricio Ospina Leguizamón.

## Tecnologías

- Java 21
- Spring Boot 4.1 (Web MVC, Data JPA, Validation)
- H2 (base de datos en memoria, con datos de ejemplo)
- JUnit 5, Mockito y MockMvc para las pruebas
- Maven
- GitHub Actions: cada cambio subido a `main` compila el proyecto y ejecuta las pruebas

## Cómo ejecutar

Requisitos: JDK 21 instalado. No hace falta instalar Maven: el proyecto trae Maven Wrapper.

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

Para ejecutar las pruebas:

```bash
./mvnw test        # Windows: mvnw.cmd test
```

Consola de la base de datos (opcional): `http://localhost:8080/h2-console` con JDBC URL `jdbc:h2:mem:polizasdb`, usuario `sa` y sin contraseña.

## Seguridad

Todas las peticiones deben incluir el header:

```
x-api-key: 123456
```

Sin el header, o con un valor distinto, la API responde `401 Unauthorized`.

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/polizas?tipo=&estado=` | Lista pólizas. Filtros opcionales: `tipo` (`INDIVIDUAL`, `COLECTIVA`) y `estado` (`ACTIVA`, `RENOVADA`, `CANCELADA`) |
| GET | `/polizas/{id}/riesgos` | Lista los riesgos de una póliza |
| POST | `/polizas/{id}/renovar` | Renueva la póliza: canon y prima aumentan según el IPC y el estado pasa a `RENOVADA` |
| POST | `/polizas/{id}/cancelar` | Cancela la póliza y todos sus riesgos |
| POST | `/polizas/{id}/riesgos` | Agrega un riesgo (solo pólizas colectivas) |
| POST | `/riesgos/{id}/cancelar` | Cancela un riesgo de una póliza colectiva |
| POST | `/core-mock/evento` | Mock del CORE: registra en el log que la operación se intentó enviar |

### Ejemplos con curl

```bash
# Listar pólizas colectivas activas
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"

# Ver los riesgos de la póliza 2
curl -H "x-api-key: 123456" http://localhost:8080/polizas/2/riesgos

# Renovar la póliza 1 (canon 1.500.000 -> 1.578.000 con IPC 5,20 %)
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/1/renovar

# Agregar un riesgo a la póliza colectiva 2
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"direccionInmueble":"Calle 80 # 20-15, Bogotá","arrendatario":"Sofía Díaz","arrendador":"Luis Castro","canonMensual":2000000}' \
  http://localhost:8080/polizas/2/riesgos

# Cancelar el riesgo 2
curl -X POST -H "x-api-key: 123456" http://localhost:8080/riesgos/2/cancelar

# Cancelar la póliza 2 (cancela también sus riesgos)
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/2/cancelar

# Probar el mock del CORE directamente
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"evento":"ACTUALIZACION","polizaId":555}' \
  http://localhost:8080/core-mock/evento
```

### Datos de ejemplo

| Id | Número | Tipo | Estado | Riesgos |
| --- | --- | --- | --- | --- |
| 1 | POL-0001 | INDIVIDUAL | ACTIVA | 1 activo |
| 2 | POL-0002 | COLECTIVA | ACTIVA | 2 activos |
| 3 | POL-0003 | INDIVIDUAL | CANCELADA | 1 cancelado |

## Reglas de negocio

- Una póliza individual solo puede tener 1 riesgo.
- Solo se pueden agregar riesgos a pólizas colectivas.
- No se puede renovar una póliza cancelada.
- La cancelación de una póliza cancela todos sus riesgos.
- El riesgo de una póliza individual no se cancela por separado: se cancela la póliza.
- Prima = canon mensual × meses de vigencia. En una colectiva, canon y prima son la suma de sus riesgos activos.
- Renovación: canon nuevo = canon × (1 + IPC). El IPC se configura en `app.renovacion.ipc-porcentaje`.

## Respuestas de error

Todos los errores tienen el mismo formato:

```json
{
  "timestamp": "2026-11-12T10:15:30",
  "status": 422,
  "error": "Unprocessable Entity",
  "mensaje": "Solo se pueden agregar riesgos a pólizas colectivas"
}
```

| Código | Cuándo |
| --- | --- |
| 400 | Datos de entrada inválidos (campos vacíos, canon negativo, filtro desconocido) |
| 401 | Falta el header `x-api-key` o es incorrecto |
| 404 | La póliza o el riesgo no existe |
| 422 | La operación viola una regla de negocio |

## Estructura del proyecto

```
src/main/java/com/danielospina/polizas
├── controller     # Endpoints REST (PolizaController, RiesgoController, CoreMockController)
├── service        # Casos de uso y transacciones (PolizaService, RiesgoService)
├── repository     # Acceso a datos con Spring Data JPA
├── model          # Entidades Poliza y Riesgo con sus reglas de negocio
├── dto            # Objetos de entrada y salida del API
├── integration    # Envío de eventos al CORE (CoreClient y su implementación HTTP)
├── config         # Filtro de seguridad x-api-key
└── exception      # Excepciones y manejador global de errores
```

## Decisiones de diseño

- **Reglas de negocio en las entidades.** `Poliza` y `Riesgo` validan sus propias reglas (por ejemplo, `poliza.renovar()` rechaza una póliza cancelada). Así ningún servicio puede saltárselas y se prueban sin base de datos.
- **Envío al CORE después de confirmar la transacción.** El servicio publica un evento y `CoreEventListener` lo envía al CORE solo cuando la transacción se confirma (`@TransactionalEventListener`). Si el CORE falla, la operación ya guardada no se revierte y el error queda en el log.
- **El CORE detrás de una interfaz.** `CoreClient` aísla la integración: para conectar el servicio real de edición en WebLogic basta con otra implementación, sin tocar el negocio.
- **Valores monetarios con `BigDecimal`**, redondeados al peso, para evitar errores de precisión.
- **DTOs en lugar de entidades** en las respuestas, para no exponer el modelo interno.

Este proyecto es la versión mínima del diseño del Módulo 1. Las diferencias son intencionales por el alcance de la prueba: H2 en lugar de PostgreSQL, `x-api-key` en lugar de OAuth2 y un mock HTTP en lugar del Adapter real con Kafka.
