-- =======================================================
-- FLYWAY MIGRATION V8: Tabla de Asistencias
-- =======================================================

CREATE TABLE IF NOT EXISTS asistencias (
    id UUID PRIMARY KEY,
    curso_id UUID NOT NULL REFERENCES cursos(id) ON DELETE CASCADE,
    materia_id UUID REFERENCES materias(id) ON DELETE SET NULL,
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Presente',
    observaciones TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_asistencia_curso_est_fecha_mat UNIQUE (curso_id, estudiante_id, fecha, materia_id)
);

CREATE INDEX IF NOT EXISTS idx_asistencias_curso_fecha ON asistencias(curso_id, fecha);
CREATE INDEX IF NOT EXISTS idx_asistencias_estudiante ON asistencias(estudiante_id);
