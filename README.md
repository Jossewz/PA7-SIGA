# SIGA IEA

Sistema Integral de Gestión Académica y Administrativa de la **Institución Educativa Ambientalista de Cartagena de Indias (IEACI)**, desarrollado con Spring Boot 3.5, PostgreSQL 16, Thymeleaf, HTMX, Alpine.js, Tailwind CSS, MinIO y el Asistente Escolar Inteligente "Mangle" (Groq Cloud / Ollama Local).

---

## Tecnologías y Stack Técnico

### Backend y Núcleo del Sistema
- **Java JDK 21 (LTS)**
- **Spring Boot 3.5.6** (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`)
- **Spring Security 6:** Control de acceso basado en roles (RBAC: `ADMIN`, `DOCENTE`, `ESTUDIANTE`, `PERSONAL_ADMINISTRATIVO`), contraseñas hasheadas con BCrypt y protección perimetral CSRF con `CookieCsrfTokenRepository` (`X-XSRF-TOKEN`).
- **Maven Wrapper:** Herramienta de compilación y empaquetado (`mvnw` / `mvnw.cmd`).
- **Lombok 1.18.38:** Reducción de código boilerplate para entidades y DTOs.

### Base de Datos y Persistencia
- **PostgreSQL 16 (16.5 / 16.x):** Motor relacional principal (imagen Docker `postgres:16`).
- **Flyway Database Migrations:** 21 scripts de migración secuenciales (`V1__` a `V21__`) con claves compuestas de integridad temporal (`V18`), restricciones condicionales de matrícula (`V19`), bitácora inmutable de auditoría con triggers (`V20`) y ponderaciones SIEACI (`V21`).
- **H2 Database:** Base de datos en memoria para ejecución rápida de pruebas unitarias y de integración de controladores.

### Almacenamiento de Objetos (Object Storage)
- **MinIO:** Servidor de almacenamiento de objetos compatible con Amazon S3 API (imagen `quay.io/minio/minio`).
- **MinIO SDK Java (8.5.14):** Gestión de documentos de matrícula, soportes en PDF y fotos de perfil con auto-aprovisionamiento del bucket `siga`.

### Capa de Presentación y Frontend
- **Thymeleaf 3:** Motor de plantillas del lado del servidor integrado con `thymeleaf-extras-springsecurity6`.
- **HTMX 4.0.3 (`htmx-spring-boot-thymeleaf`):** Navegación fluida tipo SPA, wizards asíncronos por pasos y actualización reactiva de componentes sin recargar la página.
- **Alpine.js:** Reactividad ligera en el cliente para el widget del chatbot, paneles colapsables y modales interactivos.
- **Tailwind CSS:** Diseño visual institucional compilado mediante `tailwind-maven-plugin` (1.1.0) sin requerir runtime de Node.js en desarrollo estándar.
- **Lucide Icons:** Iconografía local SVG integrada.

### Inteligencia Artificial y Asistente Escolar ("Mangle")
- **Ruta Dual con Frontera de Confianza (*Trust Boundary*):** Arquitectura de privacidad bajo la **Ley Estatutaria 1581 de 2012** y el **Decreto Único Reglamentario 1074 de 2015**. Los datos personales (PII) y herramientas transaccionales nunca salen a la nube.
- **Groq Cloud API:** Inferencia de alta velocidad para consultas institucionales y normativas con modelos de última generación (`openai/gpt-oss-120b`, `qwen/qwen3.8-27b`).
- **Ollama Local:** Inferencia on-premise local (`http://localhost:11434`) con modelo `llama3.2:1b`.
- **Motor Determinista Java Zero-Failure:** Contingencia institucional garantizada en caso de indisponibilidad de conexión o APIs de LLM.

### Calidad, Pruebas y Cobertura
- **JUnit 5 & Mockito:** Pruebas unitarias de servicios, validadores y lógica de negocio.
- **Spring Security Test & Spring MockMvc:** Verificación de filtros CSRF, autorización por roles y endpoints REST.
- **JaCoCo 0.8.12 (`jacoco-maven-plugin`):** Generación de informes y métricas de cobertura de código.
- **k6:** Pruebas de estrés y contención de concurrencia (`scripts/k6/`).

### Infraestructura y Contenerización
- **Docker & Docker Compose:** Orquestación de servicios (`db`, `app`, `minio`, `pgadmin` y perfil opcional `frontend`).
- **pgAdmin 4:** Entorno gráfico web para administración y monitoreo de PostgreSQL.

---

## Base de Datos

Credenciales por defecto (Docker):

```text
Base de datos: siga
Usuario: postgres
Password: siga
```

Puertos y conexión:

```text
PostgreSQL dentro de Docker:
Host: db
Puerto: 5432

PostgreSQL desde tu PC (IDE/local):
Host: localhost
Puerto: 5433 (o 5434 según el puerto expuesto en docker-compose)
```

URLs de conexión JDBC:

```text
Desde IDE / Local (por defecto):
jdbc:postgresql://localhost:5433/siga

Desde Docker (red interna):
jdbc:postgresql://db:5432/siga
```

> **Nota sobre puertos:** El puerto `5433` (o `5434`) se utiliza externamente para evitar colisiones si tienes otra instancia local de PostgreSQL corriendo en el puerto estándar `5432`. Si ejecutas la aplicación contra un PostgreSQL local en `5432`:
> ```bash
> SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/siga SPRING_DATASOURCE_PASSWORD=tu_password ./mvnw spring-boot:run
> ```

---

## Configuración del Asistente Escolar "Mangle" (Chatbot IA)

El asistente inteligente opera bajo una arquitectura resiliente y respeta la privacidad escolar:

| Variable de Entorno | Propiedad en `application.properties` | Valor por Defecto | Descripción |
| :--- | :--- | :--- | :--- |
| `GROQ_API_KEY` | `siga.chat.groq.api-key` | `""` (vacío) | Clave API de Groq Cloud ([console.groq.com](https://console.groq.com)). |
| `GROQ_MODEL` | `siga.chat.groq.model` | `openai/gpt-oss-120b` | Modelo activo en la nube (alternativo: `qwen/qwen3.8-27b`). |
| `GROQ_API_URL` | `siga.chat.groq.url` | `https://api.groq.com/openai/v1` | URL base de la API de Groq. |
| `OLLAMA_URL` | `siga.chat.ollama.url` | `http://localhost:11434` | Endpoint del servidor local Ollama. |
| `OLLAMA_MODEL` | `siga.chat.ollama.model` | `llama3.2:1b` | Modelo SLM local en Ollama. |

> **Funcionamiento sin LLM externo (Zero-Failure):** Si no defines `GROQ_API_KEY` o no tienes Ollama ejecutándose, **el chatbot continuará funcionando al 100%** mediante el Motor Determinista de SIGA, respondiendo consultas sobre notas, horarios, asistencias y el Manual de Convivencia sin arrojar errores.

---

## Ejecutar el Proyecto

### 1. Requisitos Previos
- Java JDK 21 instalado y configurado en el `PATH`.
- Docker y Docker Compose (opcional pero recomendado para BD y MinIO).

### 2. Iniciar Servicios de Infraestructura con Docker
Para levantar únicamente PostgreSQL y MinIO (ideal si ejecutas Spring Boot desde tu IDE):

```bash
docker compose up -d db minio
```

Para levantar todo el stack (Backend + PostgreSQL + MinIO + pgAdmin):

```bash
docker compose up --build
```

### 3. Compilar y Ejecutar Spring Boot

#### Linux / macOS
```bash
# Compilar e instalar dependencias
./mvnw clean install

# Ejecutar la aplicación
./mvnw spring-boot:run
```
*(Si `mvnw` no tiene permisos de ejecución: `chmod +x mvnw`)*

#### Windows (CMD / PowerShell)
```cmd
:: Compilar e instalar dependencias
mvnw.cmd clean install

:: Ejecutar la aplicación
mvnw.cmd spring-boot:run
```

La aplicación estará disponible en: [http://localhost:8080](http://localhost:8080)

---

## Suite de Pruebas y Calidad de Código

SIGA cuenta con una sólida batería de pruebas automatizadas:

```bash
# 1. Ejecutar pruebas unitarias, MockMvc, RBAC y de seguridad en memoria (H2)
./mvnw test

# 2. Ejecutar pruebas de integración relacional contra PostgreSQL real
./mvnw test -Pintegration

# 3. Generar informe de cobertura de código JaCoCo
./mvnw test jacoco:report
```
*El informe HTML de JaCoCo se genera en: `target/site/jacoco/index.html`.*

---

## Docker

Servicios y puertos expuestos:

```text
Backend (SIGA):       http://localhost:8080
PostgreSQL:           localhost:5433 (o 5434)
MinIO API:            http://localhost:9000
MinIO Console:        http://localhost:9001
pgAdmin 4:            http://localhost:5050
Vite (Opcional):      http://localhost:5173
```

Comandos útiles de Docker Compose:

```bash
# Detener contenedores
docker compose down

# Detener contenedores y eliminar volúmenes de datos
docker compose down -v

# Ver logs del backend
docker compose logs -f app

# Ver logs de la base de datos
docker compose logs -f db

# Verificar estado de PostgreSQL
docker compose exec db pg_isready -U postgres -d siga
```

---

## pgAdmin (Administración Gráfica de PostgreSQL)

Acceso desde el navegador:

```text
URL: http://localhost:5050
Correo: admin@siga.dev
Contraseña: admin
```

Configurar conexión al servidor en pgAdmin:

```text
Name: Docker
Host name/address: db
Port: 5432
Maintenance database: siga
Username: postgres
Password: siga
```

> **Importante:** Al conectarse desde pgAdmin dentro de Docker, el Host debe ser `db` y el puerto `5432` (resolución de red interna de Docker).

---

## MinIO (Almacenamiento de Archivos)

MinIO almacena fotos de perfil, documentos adjuntos de matrícula, soportes y certificados. El bucket `siga` se aprovisiona automáticamente en el arranque de la aplicación.

Acceso a la consola web:

```text
URL Consola: http://localhost:9001
Usuario (Root User): admin
Contraseña (Root Password): admin123
```

Conexión API:
- Desde el backend en Docker: `http://minio:9000`
- Desde tu PC / IDE local: `http://localhost:9000`

---

## Acceso al Sistema y Credenciales Iniciales

Al iniciar el backend, el servicio `DataSeeder` inicializa automáticamente el usuario Super Administrador:

```text
URL del Sistema: http://localhost:8080/login
Usuario: admin@ieaci.edu.co
Contraseña: admin
Rol: ADMIN
```

### Roles del Sistema:
* **ADMIN:** Control total del sistema, configuración institucional, años lectivos, periodos, matrícula, clases, personal, estudiantes, reportes y certificados.
* **PERSONAL_ADMINISTRATIVO:** Gestión de inscripciones, expedientes académicos, asignación de cursos, consulta de personal y expedición de certificados.
* **DOCENTE:** Registro de calificaciones, control de asistencia diaria en Mi Jornada, gestión de evaluaciones y consulta de nómina de estudiantes.
* **ESTUDIANTE:** Consulta de notas por periodo, porcentaje de asistencia, agenda de clases, citaciones disciplinarias y solicitud de certificados de estudio.

*Nota:* Las contraseñas temporales generadas automáticamente al registrar nuevos estudiantes o docentes siguen el patrón `IEACI2026*` (o el año lectivo en curso) y se almacenan de forma segura con hash BCrypt.

---

## Control de Versiones (Git)

```bash
# Ver estado de cambios
git status

# Subir cambios a tu rama
git add .
git commit -m "feat(modulo): descripcion del cambio"
git push origin tu_rama

# Traer cambios actualizados de la rama principal
git checkout feature/back
git pull origin feature/back
```

