# SIGA IEA

## Indice

- Tecnologias: linea 12
- Base de datos: linea 28
- Comandos Git: linea 57
- Comandos para correr el proyecto: linea 80
- Comandos para dockerizar y correr contenedores: linea 106
- Comandos de utilidad: linea 146

## Tecnologias

- Java JDK 21
- Spring Boot 3.5.x
- Maven Wrapper
- PostgreSQL 18.3
- Docker y Docker Compose
- Thymeleaf
- Spring Security
- Spring Data JPA
- Validation
- Lombok
- HTMX con Thymeleaf
- Node.js 22+
- Vite

## Base de datos

Credenciales del proyecto:

```text
Base de datos: siga
Usuario: postgres
Password: 1234567
```

Para usar la base de datos en Docker:

```text
Host desde la maquina: localhost
Puerto desde la maquina: 5433
Host dentro de Docker: db
Puerto dentro de Docker: 5432
URL local/IDE: jdbc:postgresql://localhost:5433/siga
URL en Docker: jdbc:postgresql://db:5432/siga
```

El puerto externo quedó en `5433` porque `5432` suele estar ocupado por PostgreSQL local. Ese cambio ya está en `docker-compose.yml`, así que al subirlo al repositorio no hay que repetirlo manualmente.

Para usar una base de datos PostgreSQL local fuera de Docker, cree la base `siga` con usuario `postgres` y password `1234567`. Si su PostgreSQL local usa el puerto `5432`, cambie temporalmente la variable:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/siga ./mvnw spring-boot:run
```

## Comandos Git

Clonar el repositorio:

```bash
git clone <url-del-repositorio>
cd PA7
```

Ver cambios pendientes:

```bash
git status
```

Subir cambios (A tu rama del repositorio): 

```bash
git add .
git commit -m "Configurar Docker y documentacion inicial"
git push
```

Bajar cambios: 

```bash
git checkout rama_origen
git pull
git checkout tu_rama
git merge rama_origen (para traer los cambios a TU rama)
```

Para mandar cambios a la main es necesario hacer un pull request desde github
y esperar que el lider de proyecto autorice el cambio para evitar conflictos
a la hora de mergear cambios y asi llevar un control mas limpios.

## Comandos para correr el proyecto

Instalar dependencias y compilar:

```bash
./mvnw clean install
```

Correr Spring Boot usando la base de datos configurada en `application.properties`:

```bash
./mvnw spring-boot:run
```

Correr Spring Boot usando PostgreSQL local en puerto `5432`:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/siga ./mvnw spring-boot:run
```

Si `mvnw` no tiene permisos de ejecucion:

```bash
chmod +x mvnw
```

## Comandos para dockerizar y correr contenedores

Levantar backend y PostgreSQL con Docker:

```bash
docker compose up --build
```

Levantar solo PostgreSQL en Docker para usar Spring Boot desde el IDE:

```bash
docker compose up -d db
```

Levantar backend, PostgreSQL y Vite cuando exista `frontend/`:

```bash
docker compose --profile frontend up --build
```

Detener contenedores:

```bash
docker compose down
```

Detener contenedores y borrar datos de PostgreSQL:

```bash
docker compose down -v
```

Servicios disponibles:

```text
Backend: http://localhost:8080
PostgreSQL Docker: localhost:5433
Vite: http://localhost:5173
```

## Comandos de utilidad

Ver configuracion final de Docker Compose:

```bash
docker compose config
```

Ver logs del backend:

```bash
docker compose logs -f app
```

Ver logs de PostgreSQL:

```bash
docker compose logs -f db
```

Reiniciar solo el backend:

```bash
docker compose restart app
```

Crear el frontend con Vite:

```bash
npm create vite@latest frontend
```

Correr Vite localmente:

```bash
cd frontend
npm install
npm run dev
```

Verificar que PostgreSQL Docker responda:

```bash
docker compose exec db pg_isready -U postgres -d siga
```

## Notas rapidas

- Al entrar a `http://localhost:8080`, Spring Security puede pedir login.
- Usuario temporal: `user`.
- La password temporal aparece en los logs como `Using generated security password`.
- Si aparece el aviso de Thymeleaf sobre `classpath:/templates/`, es normal mientras no existan vistas HTML.
