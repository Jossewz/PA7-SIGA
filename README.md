````md
# SIGA IEA

Sistema de Gestión IEA desarrollado con Spring Boot, PostgreSQL, Thymeleaf y Docker.

## Tecnologías

- Java JDK 21
- Spring Boot 3.5.x
- Maven Wrapper
- PostgreSQL 18.3
- Docker + Docker Compose
- Thymeleaf
- Spring Security
- Spring Data JPA
- Validation
- Lombok
- HTMX + Thymeleaf
- Node.js 22+
- Vite

## Base de datos

Credenciales:

```text
Base de datos: siga
Usuario: postgres
Password: 1234567
````

Puertos y conexión:

```text
PostgreSQL dentro de Docker:
Host: db
Puerto: 5432

PostgreSQL desde tu PC (IDE/local):
Host: localhost
Puerto: 5433
```

URLs:

```text
Desde IDE/local:
jdbc:postgresql://localhost:5433/siga

Desde Docker:
jdbc:postgresql://db:5432/siga
```

`5433` es el puerto expuesto en tu máquina para evitar conflictos con instalaciones locales de PostgreSQL que normalmente usan `5432`.

Si tienes PostgreSQL instalado localmente fuera de Docker:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/siga ./mvnw spring-boot:run
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

Levantar backend + PostgreSQL:

```bash
docker compose up --build
```

Levantar solo PostgreSQL (para usar Spring Boot desde el IDE):

```bash
docker compose up -d db
```

Levantar backend + PostgreSQL + frontend:

```bash
docker compose --profile frontend up --build
```

Detener contenedores:

```bash
docker compose down
```

Eliminar contenedores y datos:

```bash
docker compose down -v
```

Servicios:

```text
Backend: http://localhost:8080
PostgreSQL: localhost:5433
Vite: http://localhost:5173
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

Verificar PostgreSQL:

```bash
docker compose exec db pg_isready -U postgres -d siga
```

Crear frontend con Vite:

```bash
npm create vite@latest frontend
```

Correr frontend:

```bash
cd frontend
npm install
npm run dev
```

## pgAdmin (Interfaz gráfica PostgreSQL)

Acceder desde navegador:

```text
http://localhost:5050

Credenciales de acceso:

Correo: admin@siga.com
Contraseña: admin123

Agregar servidor PostgreSQL:

Name: Docker
Host name/address: db
Port: 5432
Maintenance database: siga
Username: postgres
Password: siga

Importante: si pgAdmin corre en Docker, el host debe ser db y no localhost, ya que ambos contenedores se comunican por la red interna de Docker.

## Notas

* Spring Security puede pedir login al entrar a `http://localhost:8080`.
* Usuario temporal: `user`.
* La contraseña temporal aparece en logs como `Using generated security password`.
* El aviso de Thymeleaf sobre `classpath:/templates/` es normal mientras no existan vistas HTML.

```