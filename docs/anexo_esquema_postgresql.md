# ANEXO TÉCNICO: Esquema Relacional de Base de Datos PostgreSQL
**Entorno:** SIGA - Institución Educativa Ambientalista de Cartagena de Indias
**Motor:** PostgreSQL 16.15 (siga-postgres en contenedor Docker / Linux Alpine)
**Base de Datos:** siga
**Extracción:** `information_schema` (Auditoría Directa y Programática)

## 1. Catálogo Completo de Tablas y Columnas
Se auditaron y extrajeron **28 tablas** del esquema `public`.

### Tabla: `acudientes`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `nombres` | `varchar(255)` | NO | `-` | - |
| `apellidos` | `varchar(255)` | NO | `-` | - |
| `parentesco` | `varchar(100)` | SÍ | `-` | - |
| `tipo_documento` | `varchar(50)` | SÍ | `-` | - |
| `numero_documento` | `varchar(100)` | SÍ | `-` | - |
| `telefono` | `varchar(50)` | SÍ | `-` | - |
| `email` | `varchar(255)` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `-` | - |
| `email_secundario` | `varchar(255)` | SÍ | `-` | - |
| `autoriza_notificaciones` | `boolean` | SÍ | `true` | - |

**Restricciones UNIQUE:**
- `acudientes_numero_documento_key`: (numero_documento)

### Tabla: `anios_lectivos`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `anio` | `integer` | NO | `-` | - |
| `estado` | `varchar(50)` | NO | `'ACTIVO'::character varying` | - |
| `fecha_inicio` | `date` | SÍ | `-` | - |
| `fecha_fin` | `date` | SÍ | `-` | - |
| `es_actual` | `boolean` | SÍ | `false` | - |
| `created_at` | `timestamp` | SÍ | `now()` | - |
| `updated_at` | `timestamp` | SÍ | `now()` | - |

**Restricciones UNIQUE:**
- `anios_lectivos_anio_key`: (anio)

### Tabla: `asistencias`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `sesion_id` | `uuid` | NO | `-` | FK -> `sesiones_clase.id` |
| `estudiante_id` | `uuid` | NO | `-` | FK -> `estudiantes.id` |
| `estado` | `varchar(20)` | NO | `'PRESENTE'::character varying` | - |
| `observaciones` | `text` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `now()` | - |
| `updated_at` | `timestamp` | SÍ | `now()` | - |

**Restricciones UNIQUE:**
- `uk_asistencia_sesion_estudiante`: (sesion_id, sesion_id, estudiante_id, estudiante_id)

**Claves Foráneas (Foreign Keys):**
- `asistencias_estudiante_id_fkey`: `(estudiante_id)` -> `estudiantes(id)`
- `asistencias_sesion_id_fkey`: `(sesion_id)` -> `sesiones_clase(id)`

### Tabla: `auditoria_cambios`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `gen_random_uuid()` | **PK** |
| `entidad` | `varchar(50)` | NO | `-` | - |
| `entidad_id` | `uuid` | NO | `-` | - |
| `campo` | `varchar(50)` | NO | `-` | - |
| `valor_anterior` | `text` | SÍ | `-` | - |
| `valor_nuevo` | `text` | SÍ | `-` | - |
| `usuario_id` | `uuid` | SÍ | `-` | - |
| `usuario_email` | `varchar(100)` | SÍ | `-` | - |
| `fecha` | `timestamp` | NO | `CURRENT_TIMESTAMP` | - |

### Tabla: `bloques`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `numero` | `integer` | NO | `-` | - |
| `nombre` | `varchar(50)` | NO | `-` | - |
| `hora_inicio` | `time` | NO | `-` | - |
| `hora_fin` | `time` | NO | `-` | - |
| `jornada` | `varchar(50)` | SÍ | `'MaÃ±ana'::character varying` | - |
| `tipo` | `varchar(50)` | SÍ | `'CLASE'::character varying` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |
| `created_at` | `timestamp` | SÍ | `now()` | - |

**Restricciones UNIQUE:**
- `uk_bloque_numero_jornada`: (numero, numero, jornada, jornada)

### Tabla: `calificaciones`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `evaluacion_id` | `uuid` | NO | `-` | FK -> `evaluaciones.id` |
| `estudiante_id` | `uuid` | NO | `-` | FK -> `estudiantes.id` |
| `nota` | `numeric(3,2)` | NO | `0.0` | - |
| `observaciones` | `text` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `-` | - |
| `updated_at` | `timestamp` | SÍ | `-` | - |

**Restricciones UNIQUE:**
- `uk_evaluacion_estudiante`: (evaluacion_id, evaluacion_id, estudiante_id, estudiante_id)

**Claves Foráneas (Foreign Keys):**
- `calificaciones_estudiante_id_fkey`: `(estudiante_id)` -> `estudiantes(id)`
- `calificaciones_evaluacion_id_fkey`: `(evaluacion_id)` -> `evaluaciones(id)`

### Tabla: `configuracion_institucional`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `nit` | `varchar(50)` | NO | `-` | - |
| `nombre_inst` | `varchar(255)` | NO | `-` | - |
| `direccion_inst` | `varchar(255)` | SÍ | `-` | - |
| `telefono_inst` | `varchar(50)` | SÍ | `-` | - |
| `correo_inst` | `varchar(150)` | SÍ | `-` | - |
| `ano_lectivo` | `varchar(10)` | NO | `'2026'::character varying` | - |
| `rector_nombre` | `varchar(150)` | SÍ | `-` | - |
| `updated_at` | `timestamp` | SÍ | `-` | - |

### Tabla: `curso_estudiante`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `curso_id` | `uuid` | NO | `-` | FK -> `cursos.id` |
| `estudiante_id` | `uuid` | NO | `-` | FK -> `estudiantes.id` |
| `ano_lectivo` | `varchar(10)` | NO | `-` | - |
| `anio_lectivo_id` | `uuid` | NO | `-` | FK -> `anios_lectivos.id` |

**Restricciones UNIQUE:**
- `uk_curso_estudiante_anio_lectivo`: (estudiante_id, estudiante_id, anio_lectivo_id, anio_lectivo_id)

**Claves Foráneas (Foreign Keys):**
- `curso_estudiante_anio_lectivo_id_fkey`: `(anio_lectivo_id)` -> `anios_lectivos(id)`
- `curso_estudiante_curso_id_fkey`: `(curso_id)` -> `cursos(id)`
- `curso_estudiante_estudiante_id_fkey`: `(estudiante_id)` -> `estudiantes(id)`
- `fk_ce_curso_anio`: `(curso_id, curso_id, anio_lectivo_id, anio_lectivo_id)` -> `cursos(anio_lectivo_id, id, id, anio_lectivo_id)`

### Tabla: `curso_materia`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `curso_id` | `uuid` | NO | `-` | FK -> `cursos.id` |
| `materia_id` | `uuid` | NO | `-` | FK -> `materias.id` |
| `docente_id` | `uuid` | SÍ | `-` | FK -> `docentes.id` |
| `ano_lectivo` | `varchar(10)` | NO | `-` | - |
| `anio_lectivo_id` | `uuid` | NO | `-` | FK -> `anios_lectivos.id` |

**Restricciones UNIQUE:**
- `uk_curso_materia_anio_lectivo`: (curso_id, curso_id, curso_id, materia_id, materia_id, materia_id, anio_lectivo_id, anio_lectivo_id, anio_lectivo_id)

**Claves Foráneas (Foreign Keys):**
- `curso_materia_anio_lectivo_id_fkey`: `(anio_lectivo_id)` -> `anios_lectivos(id)`
- `curso_materia_curso_id_fkey`: `(curso_id)` -> `cursos(id)`
- `curso_materia_docente_id_fkey`: `(docente_id)` -> `docentes(id)`
- `curso_materia_materia_id_fkey`: `(materia_id)` -> `materias(id)`
- `fk_cm_curso_anio`: `(curso_id, curso_id, anio_lectivo_id, anio_lectivo_id)` -> `cursos(id, anio_lectivo_id, anio_lectivo_id, id)`

### Tabla: `cursos`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `grado` | `varchar(50)` | NO | `-` | - |
| `grupo` | `varchar(10)` | NO | `'01'::character varying` | - |
| `jornada` | `varchar(50)` | SÍ | `'MaÃ±ana'::character varying` | - |
| `cupos_maximos` | `integer` | SÍ | `35` | - |
| `director_id` | `uuid` | SÍ | `-` | FK -> `docentes.id` |
| `ano_lectivo` | `varchar(10)` | NO | `-` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |
| `created_at` | `timestamp` | SÍ | `-` | - |
| `anio_lectivo_id` | `uuid` | NO | `-` | FK -> `anios_lectivos.id` |

**Restricciones UNIQUE:**
- `uk_curso_grado_grupo_anio_lectivo`: (grado, grado, grado, grupo, grupo, grupo, anio_lectivo_id, anio_lectivo_id, anio_lectivo_id)
- `uk_cursos_id_anio_lectivo`: (id, id, anio_lectivo_id, anio_lectivo_id)

**Claves Foráneas (Foreign Keys):**
- `cursos_anio_lectivo_id_fkey`: `(anio_lectivo_id)` -> `anios_lectivos(id)`
- `cursos_director_id_fkey`: `(director_id)` -> `docentes(id)`

### Tabla: `docentes`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `nombres` | `varchar(255)` | NO | `-` | - |
| `apellidos` | `varchar(255)` | NO | `-` | - |
| `tipo_documento` | `varchar(50)` | SÍ | `-` | - |
| `numero_documento` | `varchar(100)` | SÍ | `-` | - |
| `genero` | `varchar(50)` | SÍ | `-` | - |
| `telefono` | `varchar(50)` | SÍ | `-` | - |
| `fecha_nacimiento` | `date` | SÍ | `-` | - |
| `direccion` | `varchar(255)` | SÍ | `-` | - |
| `especialidad` | `varchar(150)` | SÍ | `-` | - |
| `titulo` | `varchar(150)` | SÍ | `-` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |
| `foto_key` | `varchar(255)` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `-` | - |
| `updated_at` | `timestamp` | SÍ | `-` | - |

**Restricciones UNIQUE:**
- `docentes_numero_documento_key`: (numero_documento)

### Tabla: `documentos`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `storage_key` | `varchar(500)` | NO | `-` | - |
| `nombre_original` | `varchar(255)` | NO | `-` | - |
| `content_type` | `varchar(100)` | SÍ | `-` | - |
| `size` | `bigint` | SÍ | `-` | - |
| `tipo_documento` | `varchar(100)` | NO | `-` | - |
| `checksum` | `varchar(255)` | SÍ | `-` | - |
| `matricula_id` | `uuid` | SÍ | `-` | FK -> `matriculas.id` |
| `estudiante_id` | `uuid` | SÍ | `-` | FK -> `estudiantes.id` |
| `docente_id` | `uuid` | SÍ | `-` | FK -> `docentes.id` |
| `personal_id` | `uuid` | SÍ | `-` | FK -> `personal_administrativo.id` |
| `fecha_creacion` | `timestamp` | SÍ | `-` | - |

**Claves Foráneas (Foreign Keys):**
- `documentos_docente_id_fkey`: `(docente_id)` -> `docentes(id)`
- `documentos_estudiante_id_fkey`: `(estudiante_id)` -> `estudiantes(id)`
- `documentos_matricula_id_fkey`: `(matricula_id)` -> `matriculas(id)`
- `documentos_personal_id_fkey`: `(personal_id)` -> `personal_administrativo(id)`

### Tabla: `escala_desempeno`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `codigo` | `varchar(50)` | NO | `-` | - |
| `nombre` | `varchar(100)` | NO | `-` | - |
| `nota_minima` | `numeric(3,2)` | NO | `-` | - |
| `nota_maxima` | `numeric(3,2)` | NO | `-` | - |
| `color_badge` | `varchar(50)` | NO | `-` | - |
| `color_hex` | `varchar(20)` | NO | `-` | - |
| `orden` | `integer` | NO | `-` | - |

**Restricciones UNIQUE:**
- `escala_desempeno_codigo_key`: (codigo)

### Tabla: `estudiante_acudiente`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `estudiante_id` | `uuid` | NO | `-` | FK -> `estudiantes.id` |
| `acudiente_id` | `uuid` | NO | `-` | FK -> `acudientes.id` |
| `es_principal` | `boolean` | SÍ | `false` | - |
| `parentesco` | `varchar(50)` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `now()` | - |

**Restricciones UNIQUE:**
- `uk_estudiante_acudiente`: (estudiante_id, estudiante_id, acudiente_id, acudiente_id)

**Claves Foráneas (Foreign Keys):**
- `estudiante_acudiente_acudiente_id_fkey`: `(acudiente_id)` -> `acudientes(id)`
- `estudiante_acudiente_estudiante_id_fkey`: `(estudiante_id)` -> `estudiantes(id)`

### Tabla: `estudiantes`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `codigo` | `varchar(100)` | SÍ | `-` | - |
| `nombres` | `varchar(255)` | NO | `-` | - |
| `apellidos` | `varchar(255)` | NO | `-` | - |
| `tipo_documento` | `varchar(50)` | SÍ | `-` | - |
| `numero_documento` | `varchar(100)` | SÍ | `-` | - |
| `genero` | `varchar(50)` | SÍ | `-` | - |
| `telefono` | `varchar(50)` | SÍ | `-` | - |
| `fecha_nacimiento` | `date` | SÍ | `-` | - |
| `direccion` | `varchar(255)` | SÍ | `-` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |
| `foto_key` | `varchar(255)` | SÍ | `-` | - |
| `acudiente_id` | `uuid` | SÍ | `-` | FK -> `acudientes.id` |
| `created_at` | `timestamp` | SÍ | `-` | - |
| `updated_at` | `timestamp` | SÍ | `-` | - |

**Restricciones UNIQUE:**
- `estudiantes_codigo_key`: (codigo)
- `estudiantes_numero_documento_key`: (numero_documento)

**Claves Foráneas (Foreign Keys):**
- `estudiantes_acudiente_id_fkey`: `(acudiente_id)` -> `acudientes(id)`

### Tabla: `evaluaciones`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `curso_materia_id` | `uuid` | NO | `-` | FK -> `curso_materia.id` |
| `nombre` | `varchar(255)` | NO | `-` | - |
| `periodo` | `integer` | NO | `-` | - |
| `peso` | `numeric(5,2)` | NO | `-` | - |
| `fecha` | `date` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `-` | - |

**Claves Foráneas (Foreign Keys):**
- `evaluaciones_curso_materia_id_fkey`: `(curso_materia_id)` -> `curso_materia(id)`

### Tabla: `horarios`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `curso_id` | `uuid` | NO | `-` | FK -> `cursos.id` |
| `materia_id` | `uuid` | SÍ | `-` | FK -> `materias.id` |
| `dia_semana` | `varchar(20)` | NO | `-` | - |
| `hora_inicio` | `time` | NO | `-` | - |
| `hora_fin` | `time` | NO | `-` | - |
| `salon` | `varchar(50)` | SÍ | `-` | - |
| `docente_id` | `uuid` | SÍ | `-` | FK -> `docentes.id` |
| `salon_id` | `uuid` | SÍ | `-` | FK -> `salones.id` |
| `bloque_id` | `uuid` | SÍ | `-` | FK -> `bloques.id` |

**Claves Foráneas (Foreign Keys):**
- `horarios_bloque_id_fkey`: `(bloque_id)` -> `bloques(id)`
- `horarios_curso_id_fkey`: `(curso_id)` -> `cursos(id)`
- `horarios_docente_id_fkey`: `(docente_id)` -> `docentes(id)`
- `horarios_materia_id_fkey`: `(materia_id)` -> `materias(id)`
- `horarios_salon_id_fkey`: `(salon_id)` -> `salones(id)`

### Tabla: `materias`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `nombre` | `varchar(255)` | NO | `-` | - |
| `area` | `varchar(150)` | SÍ | `-` | - |
| `intensidad_horaria` | `integer` | SÍ | `4` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |

### Tabla: `matriculas`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `estudiante_id` | `uuid` | NO | `-` | FK -> `estudiantes.id` |
| `grado` | `varchar(100)` | NO | `-` | - |
| `salon` | `varchar(50)` | SÍ | `'01'::character varying` | - |
| `ano_lectivo` | `varchar(50)` | NO | `-` | - |
| `estado` | `varchar(50)` | NO | `-` | - |
| `fecha_matricula` | `date` | SÍ | `-` | - |
| `curso_id` | `uuid` | SÍ | `-` | FK -> `cursos.id` |
| `autoriza_tratamiento_datos` | `boolean` | SÍ | `true` | - |
| `fecha_autorizacion_datos` | `timestamp` | SÍ | `now()` | - |
| `autorizado_por_nombre` | `varchar` | SÍ | `-` | - |
| `autorizado_por_documento` | `varchar` | SÍ | `-` | - |
| `anio_lectivo_id` | `uuid` | NO | `-` | FK -> `cursos.anio_lectivo_id` |

**Restricciones UNIQUE:**
- `uk_matriculas_estudiante_anio_lectivo`: (estudiante_id, estudiante_id, anio_lectivo_id, anio_lectivo_id)

**Claves Foráneas (Foreign Keys):**
- `fk_mat_curso_anio`: `(curso_id, curso_id, anio_lectivo_id, anio_lectivo_id)` -> `cursos(id, anio_lectivo_id, anio_lectivo_id, id)`
- `matriculas_anio_lectivo_id_fkey`: `(anio_lectivo_id)` -> `anios_lectivos(id)`
- `matriculas_curso_id_fkey`: `(curso_id)` -> `cursos(id)`
- `matriculas_estudiante_id_fkey`: `(estudiante_id)` -> `estudiantes(id)`

### Tabla: `periodos_academicos`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `numero_periodo` | `integer` | NO | `-` | - |
| `nombre` | `varchar(100)` | NO | `-` | - |
| `peso_porcentaje` | `numeric(5,2)` | NO | `-` | - |
| `fecha_inicio` | `date` | NO | `-` | - |
| `fecha_fin` | `date` | NO | `-` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |
| `anio_lectivo_id` | `uuid` | NO | `-` | FK -> `anios_lectivos.id` |

**Restricciones UNIQUE:**
- `uk_periodo_anio_numero`: (anio_lectivo_id, anio_lectivo_id, numero_periodo, numero_periodo)

**Claves Foráneas (Foreign Keys):**
- `periodos_academicos_anio_lectivo_id_fkey`: `(anio_lectivo_id)` -> `anios_lectivos(id)`

### Tabla: `personal_administrativo`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `nombres` | `varchar(255)` | NO | `-` | - |
| `apellidos` | `varchar(255)` | NO | `-` | - |
| `tipo_documento` | `varchar(50)` | SÍ | `-` | - |
| `numero_documento` | `varchar(100)` | SÍ | `-` | - |
| `cargo` | `varchar(150)` | SÍ | `-` | - |
| `area` | `varchar(150)` | SÍ | `-` | - |
| `genero` | `varchar(50)` | SÍ | `-` | - |
| `telefono` | `varchar(50)` | SÍ | `-` | - |
| `fecha_nacimiento` | `date` | SÍ | `-` | - |
| `direccion` | `varchar(255)` | SÍ | `-` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |
| `foto_key` | `varchar(255)` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `-` | - |
| `updated_at` | `timestamp` | SÍ | `-` | - |

**Restricciones UNIQUE:**
- `personal_administrativo_numero_documento_key`: (numero_documento)

### Tabla: `reportes`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `codigo` | `varchar(50)` | NO | `-` | - |
| `estudiante_id` | `uuid` | NO | `-` | FK -> `estudiantes.id` |
| `docente_id` | `uuid` | NO | `-` | FK -> `docentes.id` |
| `categoria` | `varchar(100)` | NO | `-` | - |
| `razon` | `varchar(150)` | NO | `-` | - |
| `descripcion_razon` | `text` | SÍ | `-` | - |
| `detalles` | `text` | NO | `-` | - |
| `estado` | `varchar(50)` | NO | `'Pendiente'::character varying` | - |
| `fecha_citacion` | `timestamp` | SÍ | `-` | - |
| `requiere_acudiente` | `boolean` | SÍ | `false` | - |
| `observaciones_admin` | `text` | SÍ | `-` | - |
| `atendido_por` | `uuid` | SÍ | `-` | FK -> `personal_administrativo.id` |
| `created_at` | `timestamp` | SÍ | `-` | - |
| `updated_at` | `timestamp` | SÍ | `-` | - |

**Restricciones UNIQUE:**
- `reportes_codigo_key`: (codigo)

**Claves Foráneas (Foreign Keys):**
- `reportes_atendido_por_fkey`: `(atendido_por)` -> `personal_administrativo(id)`
- `reportes_docente_id_fkey`: `(docente_id)` -> `docentes(id)`
- `reportes_estudiante_id_fkey`: `(estudiante_id)` -> `estudiantes(id)`

### Tabla: `rol_permisos`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `rol` | `varchar(100)` | NO | `-` | - |
| `modulo` | `varchar(100)` | NO | `-` | - |
| `nombre_modulo` | `varchar(150)` | NO | `-` | - |
| `descripcion` | `varchar(255)` | SÍ | `-` | - |
| `puede_acceder` | `boolean` | SÍ | `true` | - |
| `puede_editar` | `boolean` | SÍ | `true` | - |
| `puede_eliminar` | `boolean` | SÍ | `false` | - |

**Restricciones UNIQUE:**
- `uk_rol_modulo`: (rol, rol, modulo, modulo)

### Tabla: `salones`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `codigo` | `varchar(50)` | NO | `-` | - |
| `nombre` | `varchar(100)` | NO | `-` | - |
| `capacidad` | `integer` | SÍ | `35` | - |
| `edificio` | `varchar(100)` | SÍ | `'Bloque Principal'::character varying` | - |
| `tipo` | `varchar(50)` | SÍ | `'AULA'::character varying` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |
| `created_at` | `timestamp` | SÍ | `now()` | - |
| `updated_at` | `timestamp` | SÍ | `now()` | - |

**Restricciones UNIQUE:**
- `salones_codigo_key`: (codigo)

### Tabla: `sesiones_clase`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `curso_id` | `uuid` | NO | `-` | FK -> `cursos.id` |
| `curso_materia_id` | `uuid` | SÍ | `-` | FK -> `curso_materia.id` |
| `horario_id` | `uuid` | SÍ | `-` | FK -> `horarios.id` |
| `docente_id` | `uuid` | SÍ | `-` | FK -> `docentes.id` |
| `fecha` | `date` | NO | `-` | - |
| `hora_inicio` | `time` | NO | `-` | - |
| `hora_fin` | `time` | NO | `-` | - |
| `estado` | `varchar(20)` | NO | `'DICTADA'::character varying` | - |
| `tipo` | `varchar(20)` | NO | `'ASIGNATURA'::character varying` | - |
| `tema` | `varchar(255)` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `now()` | - |
| `updated_at` | `timestamp` | SÍ | `now()` | - |

**Restricciones UNIQUE:**
- `uk_sesion_curso_fecha_hora`: (curso_id, curso_id, curso_id, fecha, fecha, fecha, hora_inicio, hora_inicio, hora_inicio)

**Claves Foráneas (Foreign Keys):**
- `sesiones_clase_curso_id_fkey`: `(curso_id)` -> `cursos(id)`
- `sesiones_clase_curso_materia_id_fkey`: `(curso_materia_id)` -> `curso_materia(id)`
- `sesiones_clase_docente_id_fkey`: `(docente_id)` -> `docentes(id)`
- `sesiones_clase_horario_id_fkey`: `(horario_id)` -> `horarios(id)`

### Tabla: `solicitudes_certificado`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `codigo` | `varchar(50)` | NO | `-` | - |
| `estudiante_id` | `uuid` | NO | `-` | FK -> `estudiantes.id` |
| `tipo` | `varchar(255)` | NO | `-` | - |
| `categoria` | `varchar(150)` | SÍ | `-` | - |
| `motivo` | `text` | SÍ | `-` | - |
| `ano_lectivo` | `varchar(10)` | SÍ | `-` | - |
| `grado_referencia` | `varchar(50)` | SÍ | `-` | - |
| `estado` | `varchar(50)` | NO | `'Pendiente'::character varying` | - |
| `mensaje_respuesta` | `text` | SÍ | `-` | - |
| `archivo_adjunto_key` | `varchar(500)` | SÍ | `-` | - |
| `respondido_por` | `uuid` | SÍ | `-` | FK -> `personal_administrativo.id` |
| `created_at` | `timestamp` | SÍ | `-` | - |
| `updated_at` | `timestamp` | SÍ | `-` | - |

**Restricciones UNIQUE:**
- `solicitudes_certificado_codigo_key`: (codigo)

**Claves Foráneas (Foreign Keys):**
- `solicitudes_certificado_estudiante_id_fkey`: `(estudiante_id)` -> `estudiantes(id)`
- `solicitudes_certificado_respondido_por_fkey`: `(respondido_por)` -> `personal_administrativo(id)`

### Tabla: `usuarios`
| Columna | Tipo de Dato | Nulable | Predeterminado | Clave |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `uuid` | NO | `-` | **PK** |
| `email` | `varchar(255)` | NO | `-` | - |
| `password` | `varchar(255)` | SÍ | `-` | - |
| `rol` | `varchar(100)` | NO | `-` | - |
| `numero_documento` | `varchar(100)` | NO | `-` | - |
| `estado` | `varchar(50)` | SÍ | `'Activo'::character varying` | - |
| `ultimo_acceso` | `timestamp` | SÍ | `-` | - |
| `created_at` | `timestamp` | SÍ | `-` | - |

**Restricciones UNIQUE:**
- `usuarios_email_key`: (email)
- `usuarios_numero_documento_key`: (numero_documento)
