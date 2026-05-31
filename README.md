# SIGA IEA

## Configuración del Entorno de Desarrollo

Para ejecutar correctamente el proyecto, se recomienda instalar las siguientes herramientas y extensiones según el entorno de desarrollo utilizado.

### Requisitos Previos

Antes de abrir el proyecto, asegúrese de tener instalado:

- Java JDK 21 o superior
- Maven 3.9+ (opcional si el IDE ya lo gestiona internamente)
- Docker y Docker Compose
- PostgreSQL 18.3
- Node.js 22+ y npm
- Vite para el frontend

---

## Base de Datos

El proyecto usa PostgreSQL 18.3 con la siguiente configuración por defecto:

```text
Base de datos: siga
Usuario: postgres
Password: 1234567
Puerto: 5432
```

La aplicación Spring Boot lee estos valores desde variables de entorno, pero también tiene valores por defecto para trabajar en local:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/siga
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=1234567
```

---

## Docker

La configuración incluida está pensada para desarrollo. El código del proyecto se monta como volumen dentro del contenedor, por lo que no es necesario reconstruir la imagen cada vez que cambie el código. Spring Boot DevTools ayuda con reinicios durante desarrollo; si cambia código Java dentro del contenedor y no se recompila automáticamente, reinicie solo el servicio `app`.

### Levantar backend y base de datos

```bash
docker compose up --build
```

Servicios disponibles:

```text
Backend Spring Boot: http://localhost:8080
PostgreSQL: localhost:5432
```

### Levantar solo la base de datos

Use este modo si quiere ejecutar Spring Boot desde el IDE o desde la terminal local:

```bash
docker compose up -d db
```

Luego ejecute la aplicación:

```bash
./mvnw spring-boot:run
```

Si `mvnw` no tiene permisos de ejecución en Linux/macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### Ver logs

```bash
docker compose logs -f app
docker compose logs -f db
```

### Reiniciar solo la aplicación

```bash
docker compose restart app
```

### Detener contenedores

```bash
docker compose down
```

### Detener y borrar los datos de PostgreSQL

Use este comando solo si quiere reiniciar la base de datos desde cero:

```bash
docker compose down -v
```

---

## Frontend con Vite

El repositorio todavía no incluye una carpeta frontend. Cuando se cree, se recomienda ubicarla en:

```text
frontend/
```

La configuración de Docker Compose ya incluye un servicio opcional para Vite usando Node.js 22. Este servicio se activa con el perfil `frontend`:

```bash
docker compose --profile frontend up --build
```

Servicios disponibles con el perfil frontend:

```text
Backend Spring Boot: http://localhost:8080
Frontend Vite: http://localhost:5173
PostgreSQL: localhost:5432
```

Vite queda configurado para escuchar dentro del contenedor con `--host 0.0.0.0`, y el código se monta como volumen. Esto permite modificar archivos del frontend y ver los cambios inmediatamente sin hacer rebuild.

Si aún no existe el frontend, se puede crear con:

```bash
npm create vite@latest frontend
```

Después de crear el proyecto Vite, entre a `frontend/`, instale dependencias si trabaja localmente, o use directamente el perfil de Docker:

```bash
cd frontend
npm install
npm run dev
```

## Visual Studio Code

Si el proyecto se trabajará en **Visual Studio Code**, se recomienda instalar las siguientes extensiones:

### Extensiones obligatorias

- **Extension Pack for Java**  
  Proporciona soporte completo para desarrollo Java, depuración, ejecución y Maven.

- **Spring Boot Extension Pack**  
  Facilita el desarrollo con Spring Boot, incluyendo navegación, ejecución del proyecto y autocompletado.

- **Thymeleaf**  
  Proporciona soporte para vistas Thymeleaf (`th:text`, `th:if`, `th:each`, etc.).

### Extensiones recomendadas

- **Auto Rename Tag**  
  Renombra automáticamente etiquetas HTML de apertura y cierre.

- **Path Intellisense**  
  Autocompletado de rutas de archivos.

- **SQLTools** + Driver de PostgreSQL  
  Permite gestionar la base de datos PostgreSQL directamente desde VS Code.

- **Error Lens**  
  Muestra errores y advertencias directamente sobre el código.

---

## IntelliJ IDEA

Si el proyecto se trabajará en **IntelliJ IDEA**, se recomienda utilizar la versión **Community** o **Ultimate**.

### Plugins recomendados

Normalmente IntelliJ ya incluye soporte para Java y Maven, pero se recomienda verificar:

- **Spring Boot Support**  
  Soporte para proyectos Spring Boot.

- **Thymeleaf Plugin**  
  Resaltado de sintaxis y soporte para plantillas Thymeleaf.

- **Database Tools** *(Ultimate o plugin equivalente)*  
  Para conexión y administración de PostgreSQL desde el IDE.

### Configuración recomendada

Verificar que IntelliJ esté utilizando:

```text
JDK 21
```

y que Maven esté correctamente sincronizado al abrir el proyecto.

---

## Importante

Después de clonar el proyecto, ejecutar:

```bash
./mvnw clean install
```

para descargar dependencias y compilar correctamente el proyecto antes de ejecutarlo.
