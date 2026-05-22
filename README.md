## Configuración del Entorno de Desarrollo

Para ejecutar correctamente el proyecto, se recomienda instalar las siguientes herramientas y extensiones según el entorno de desarrollo utilizado.

### Requisitos Previos

Antes de abrir el proyecto, asegúrese de tener instalado:

- Java JDK 21 o superior
- Maven 3.9+ (opcional si el IDE ya lo gestiona internamente)
- PostgreSQL 16+ (o la versión definida para el proyecto)

---

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
mvn clean install
```

para descargar dependencias y compilar correctamente el proyecto antes de ejecutarlo.
