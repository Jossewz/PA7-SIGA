-- =======================================================
-- FLYWAY MIGRATION V2: Ensure column 'apellidos' exists on 'estudiantes'
-- =======================================================

ALTER TABLE estudiantes ADD COLUMN IF NOT EXISTS apellidos VARCHAR(255) DEFAULT '';
