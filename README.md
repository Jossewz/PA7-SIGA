# SIGA IEA

Sistema de Gestión Académica y Administrativa IEA desarrollado con Spring Boot, PostgreSQL, Thymeleaf, HTMX, Tailwind CSS y Docker.

## Tecnologías

- Java JDK 21
- Spring Boot 3.5.x
- Maven Wrapper
- PostgreSQL 18.3
- Flyway (Migraciones de Base de Datos)
- MinIO (Object Storage compatible con Amazon S3)
- Docker + Docker Compose
- Thymeleaf + Spring Security Extras
- HTMX (htmx-spring-boot-thymeleaf)
- Tailwind CSS (tailwind-maven-plugin)
- Spring Security (RBAC con BCrypt)
- Spring Data JPA
- Bean Validation
- Lombok

## Base de datos

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
Puerto: 5433
```

URLs de conexión JDBC:

```text
Desde IDE/local:
jdbc:postgresql://localhost:5433/siga

Desde Docker:
jdbc:postgresql://db:5432/siga
```

`5433` es el puerto expuesto en tu máquina para evitar conflictos con instalaciones locales de PostgreSQL que normalmente usan `5432`.

Si tienes PostgreSQL instalado localmente fuera de Docker (ejemplo con puerto 5432 o contraseña propia):

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/siga SPRING_DATASOURCE_PASSWORD=tu_password ./mvnw spring-boot:run
```

## Git

Clonar repositorio:

```bash
git clone <url-del-repositorio>
cd PA7
```

Ver cambios:

```bash
git status
```

Subir cambios a tu rama:

```bash
git add .
git commit -m "mensaje"
git push
```

Traer cambios de otra rama:

```bash
git checkout rama_origen
git pull
git checkout tu_rama
git merge rama_origen
```

Para enviar cambios a `main` se debe crear un Pull Request y esperar aprobación.

## Ejecutar proyecto

Compilar e instalar dependencias:

### Linux / macOS

```bash
./mvnw clean install
```

Ejecutar Spring Boot:

```bash
./mvnw spring-boot:run
```

Si `mvnw` no tiene permisos:

```bash
chmod +x mvnw
```

### Windows

Compilar:

```cmd
mvnw.cmd clean install
```

Ejecutar:

```cmd
mvnw.cmd spring-boot:run
```

## Docker

Levantar backend + PostgreSQL + MinIO:

```bash
docker compose up --build
```

Levantar solo PostgreSQL y MinIO (para ejecutar Spring Boot desde el IDE):

```bash
docker compose up -d db minio
```

Levantar todo incluyendo perfil de frontend:

```bash
docker compose --profile frontend up --build
```

Detener contenedores:

```bash
docker compose down
```

Eliminar contenedores y volúmenes de datos:

```bash
docker compose down -v
```

Servicios y puertos expuestos:

```text
Backend (SIGA): http://localhost:8080
PostgreSQL: localhost:5433
pgAdmin 4: http://localhost:5050
MinIO Console: http://localhost:9001
MinIO API: http://localhost:9000
Vite (Opcional): http://localhost:5173
```

## Comandos útiles

Ver logs del backend:

```bash
docker compose logs -f app
```

Ver logs de PostgreSQL:

```bash
docker compose logs -f db
```

Reiniciar backend:

```bash
docker compose restart app
```

Verificar estado de PostgreSQL:

```bash
docker compose exec db pg_isready -U postgres -d siga
```

## pgAdmin (Interfaz gráfica PostgreSQL)

Acceder desde navegador:

```text
URL: http://localhost:5050

Credenciales de acceso a pgAdmin:
Correo: admin@siga.dev
Contraseña: admin
```

Agregar servidor PostgreSQL en pgAdmin:

```text
Name: Docker
Host name/address: db
Port: 5432
Maintenance database: siga
Username: postgres
Password: siga
```

> **Importante:** Si pgAdmin corre en Docker, el host debe ser `db` y no `localhost`, ya que ambos contenedores se comunican por la red interna de Docker.

## MinIO (Almacenamiento de Archivos)

MinIO es un servidor de almacenamiento de objetos compatible con Amazon S3. Se utiliza para guardar fotos de perfil, documentos de matrícula, boletines y certificados.

El bucket `siga` se crea **automáticamente** al arrancar la aplicación si no existe.

Acceder a la consola web desde el navegador:

```text
URL Consola: http://localhost:9001

Credenciales de acceso:
Usuario (Root User): admin
Contraseña (Root Password): admin123
```

Puertos y conexión:

```text
MinIO API (desde la app o backend):
Host dentro de Docker: http://minio:9000
Host desde tu PC (IDE/local): http://localhost:9000

MinIO Console (consola web en navegador):
Host: http://localhost:9001
```

## Acceso al Sistema y Credenciales Iniciales

Al iniciar el backend, el servicio `DataSeeder` inicializa automáticamente el usuario Super Administrador:

```text
URL del Sistema: http://localhost:8080/login
Usuario: admin@ieaci.edu.co
Contraseña: admin
Rol: ADMIN
```

### Roles del Sistema:
* **ADMIN:** Acceso total a configuración, matrícula, personal, estudiantes, clases, reportes y certificados.
* **PERSONAL_ADMINISTRATIVO:** Gestión de matrícula, expedientes, personal, estudiantes y certificados.
* **DOCENTE:** Consulta de clases, estudiantes asignados, calificaciones y asistencia.
* **ESTUDIANTE:** Consulta de boletines, reportes y solicitud de certificados.

*Nota:* Las contraseñas temporales generadas automáticamente al registrar nuevos estudiantes o docentes siguen el patrón `IEACI2026*` (o el año lectivo en curso) y se almacenan de forma segura con hash BCrypt.
