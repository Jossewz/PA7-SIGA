-- =======================================================
-- FLYWAY MIGRATION V4: Evaluaciones y Calificaciones
-- =======================================================

CREATE TABLE IF NOT EXISTS evaluaciones (
    id UUID PRIMARY KEY,
    curso_materia_id UUID NOT NULL REFERENCES curso_materia(id),
    nombre VARCHAR(255) NOT NULL,
    periodo INT NOT NULL,
    peso DECIMAL(5,2) NOT NULL,
    fecha DATE,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS calificaciones (
    id UUID PRIMARY KEY,
    evaluacion_id UUID NOT NULL REFERENCES evaluaciones(id),
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id),
    nota DECIMAL(3,2) NOT NULL DEFAULT 0.0,
    observaciones TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_evaluacion_estudiante UNIQUE(evaluacion_id, estudiante_id)
);
