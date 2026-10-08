-- =======================================================
-- FLYWAY MIGRATION V12: Entidad AnioLectivo y Vinculacion con Periodos Academicos
-- =======================================================

-- 1. Crear tabla anios_lectivos
CREATE TABLE IF NOT EXISTS anios_lectivos (
    id UUID PRIMARY KEY,
    anio INT NOT NULL UNIQUE,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    fecha_inicio DATE,
    fecha_fin DATE,
    es_actual BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- 2. Insertar anio lectivo inicial 2026 como actual
INSERT INTO anios_lectivos (id, anio, estado, fecha_inicio, fecha_fin, es_actual, created_at, updated_at)
VALUES (
    'e0000000-0000-0000-0000-000000002026',
    2026,
    'ACTIVO',
    '2026-01-15',
    '2026-11-30',
    true,
    NOW(),
    NOW()
) ON CONFLICT (anio) DO NOTHING;

-- 3. Agregar columna anio_lectivo_id a periodos_academicos
ALTER TABLE periodos_academicos 
    ADD COLUMN IF NOT EXISTS anio_lectivo_id UUID REFERENCES anios_lectivos(id);

-- 4. Asociar los periodos existentes al anio lectivo 2026
UPDATE periodos_academicos 
SET anio_lectivo_id = 'e0000000-0000-0000-0000-000000002026'
WHERE anio_lectivo_id IS NULL;

-- 5. Hacer anio_lectivo_id NOT NULL
ALTER TABLE periodos_academicos 
    ALTER COLUMN anio_lectivo_id SET NOT NULL;

-- 6. Eliminar restriccion unique global sobre numero_periodo para permitir historial por anio
ALTER TABLE periodos_academicos 
    DROP CONSTRAINT IF EXISTS periodos_academicos_numero_periodo_key;

-- 7. Crear restriccion unique compuesta (anio_lectivo_id, numero_periodo)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_periodo_anio_numero'
    ) THEN
        ALTER TABLE periodos_academicos 
            ADD CONSTRAINT uk_periodo_anio_numero UNIQUE (anio_lectivo_id, numero_periodo);
    END IF;
END $$;
