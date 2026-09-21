-- =======================================================
-- FLYWAY MIGRATION V11: Remover direccion de acudientes
-- =======================================================

ALTER TABLE acudientes DROP COLUMN IF EXISTS direccion;
