-- =======================================================
-- FLYWAY MIGRATION V14: Relacion N:M Estudiante - Acudiente
-- =======================================================

-- 1. Crear tabla intermedia estudiante_acudiente
CREATE TABLE IF NOT EXISTS estudiante_acudiente (
    id UUID PRIMARY KEY,
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id) ON DELETE CASCADE,
    acudiente_id UUID NOT NULL REFERENCES acudientes(id) ON DELETE CASCADE,
    es_principal BOOLEAN DEFAULT false,
    parentesco VARCHAR(50),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_estudiante_acudiente UNIQUE(estudiante_id, acudiente_id)
);

-- 2. Migrar datos historicos desde estudiantes.acudiente_id
-- NOTA: NO se elimina la columna acudiente_id de estudiantes para preservar compatibilidad progresiva
INSERT INTO estudiante_acudiente (id, estudiante_id, acudiente_id, es_principal, parentesco, created_at)
SELECT 
    gen_random_uuid(),
    e.id,
    e.acudiente_id,
    true,
    COALESCE(a.parentesco, 'Acudiente Principal'),
    NOW()
FROM estudiantes e
JOIN acudientes a ON e.acudiente_id = a.id
WHERE e.acudiente_id IS NOT NULL
ON CONFLICT (estudiante_id, acudiente_id) DO NOTHING;
