-- =======================================================
-- FLYWAY MIGRATION V17: Trazabilidad Ley 1581 (Habeas Data) en Matrículas
-- =======================================================

ALTER TABLE matriculas
    ADD COLUMN IF NOT EXISTS autorizado_por_nombre VARCHAR,
    ADD COLUMN IF NOT EXISTS autorizado_por_documento VARCHAR;
