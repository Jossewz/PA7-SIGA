-- =======================================================
-- FLYWAY MIGRATION V15: Vinculacion formal de Matricula con Curso y Consentimiento Ley 1581
-- =======================================================

-- 1. Agregar columna curso_id en matriculas
ALTER TABLE matriculas 
    ADD COLUMN IF NOT EXISTS curso_id UUID REFERENCES cursos(id);

-- 2. Agregar campos para Consentimiento de Tratamiento de Datos (Ley 1581) en matriculas
ALTER TABLE matriculas 
    ADD COLUMN IF NOT EXISTS autoriza_tratamiento_datos BOOLEAN DEFAULT true;

ALTER TABLE matriculas 
    ADD COLUMN IF NOT EXISTS fecha_autorizacion_datos TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();

-- 3. Agregar correo alterno y autorizacion de notificaciones en acudientes
ALTER TABLE acudientes 
    ADD COLUMN IF NOT EXISTS email_secundario VARCHAR(255);

ALTER TABLE acudientes 
    ADD COLUMN IF NOT EXISTS autoriza_notificaciones BOOLEAN DEFAULT true;

-- 4. Backfill de curso_id para matriculas existentes
UPDATE matriculas m
SET curso_id = c.id
FROM cursos c
WHERE m.curso_id IS NULL
  AND (
      c.grado = m.grado 
      OR c.grado = m.grado || '°' 
      OR c.grado = REPLACE(m.grado, '°', '') || '°'
      OR c.grado = 'Grado ' || REPLACE(m.grado, '°', '')
  )
  AND c.grupo = COALESCE(m.salon, '01');
