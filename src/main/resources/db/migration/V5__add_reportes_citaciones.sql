-- =======================================================
-- FLYWAY MIGRATION V5: Reportes y Citaciones
-- =======================================================

CREATE TABLE IF NOT EXISTS reportes (
    id UUID PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id),
    docente_id UUID NOT NULL REFERENCES docentes(id),
    categoria VARCHAR(100) NOT NULL,
    razon VARCHAR(150) NOT NULL,
    descripcion_razon TEXT,
    detalles TEXT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
    fecha_citacion TIMESTAMP WITHOUT TIME ZONE,
    requiere_acudiente BOOLEAN DEFAULT FALSE,
    observaciones_admin TEXT,
    atendido_por UUID REFERENCES personal_administrativo(id),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
