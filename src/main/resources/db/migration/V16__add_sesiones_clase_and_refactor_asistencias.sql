-- =======================================================
-- FLYWAY MIGRATION V16: Sesiones de Clase y Refactorización de Asistencias
-- =======================================================

-- 1. Asegurar que bloque_id en horarios sea nullable
ALTER TABLE horarios ALTER COLUMN bloque_id DROP NOT NULL;

-- 2. Crear tabla sesiones_clase
CREATE TABLE IF NOT EXISTS sesiones_clase (
    id UUID PRIMARY KEY,
    curso_id UUID NOT NULL REFERENCES cursos(id) ON DELETE RESTRICT,
    curso_materia_id UUID REFERENCES curso_materia(id) ON DELETE SET NULL,   -- NULL si tipo = JORNADA
    horario_id UUID REFERENCES horarios(id) ON DELETE SET NULL,        -- NULL si es sesión extraordinaria
    docente_id UUID REFERENCES docentes(id) ON DELETE SET NULL,        -- docente efectivo
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'DICTADA',                     -- DICTADA | CANCELADA | REEMPLAZO
    tipo VARCHAR(20) NOT NULL DEFAULT 'ASIGNATURA',                    -- JORNADA | ASIGNATURA
    tema VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT ck_sesion_horas CHECK (hora_fin > hora_inicio),
    CONSTRAINT uk_sesion_curso_fecha_hora UNIQUE (curso_id, fecha, hora_inicio)
);

-- 3. Recrear tabla asistencias vinculada a sesiones_clase (reemplazando datos de prueba previos)
DROP TABLE IF EXISTS asistencias CASCADE;

CREATE TABLE asistencias (
    id UUID PRIMARY KEY,
    sesion_id UUID NOT NULL REFERENCES sesiones_clase(id) ON DELETE CASCADE,
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id) ON DELETE RESTRICT,
    estado VARCHAR(20) NOT NULL DEFAULT 'PRESENTE', -- PRESENTE | AUSENTE | TARDE | EXCUSADO
    observaciones TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_asistencia_sesion_estudiante UNIQUE (sesion_id, estudiante_id)
);

-- 4. Índices para consultas eficientes de apertura, visualización y registro
CREATE INDEX IF NOT EXISTS idx_sesiones_curso_fecha ON sesiones_clase(curso_id, fecha);
CREATE INDEX IF NOT EXISTS idx_sesiones_docente_fecha ON sesiones_clase(docente_id, fecha);
CREATE INDEX IF NOT EXISTS idx_sesiones_curso_materia ON sesiones_clase(curso_materia_id);
CREATE INDEX IF NOT EXISTS idx_asistencias_sesion ON asistencias(sesion_id);
CREATE INDEX IF NOT EXISTS idx_asistencias_estudiante ON asistencias(estudiante_id);
