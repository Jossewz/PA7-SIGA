-- =======================================================
-- FLYWAY MIGRATION V6: Solicitudes de Certificados
-- =======================================================

CREATE TABLE IF NOT EXISTS solicitudes_certificado (
    id UUID PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id),
    tipo VARCHAR(255) NOT NULL,
    categoria VARCHAR(150),
    motivo TEXT,
    ano_lectivo VARCHAR(10),
    grado_referencia VARCHAR(50),
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
    mensaje_respuesta TEXT,
    archivo_adjunto_key VARCHAR(500),
    respondido_por UUID REFERENCES personal_administrativo(id),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
