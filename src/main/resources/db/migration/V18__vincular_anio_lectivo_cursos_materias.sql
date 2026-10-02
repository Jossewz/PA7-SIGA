-- V18__vincular_anio_lectivo_cursos_materias.sql
-- Antes de aplicar: pg_dump de la BD (Flyway Community no tiene "down").

-- 1. Validar formato del año en todas las tablas
DO $$
DECLARE v_invalidos TEXT;
BEGIN
    SELECT string_agg(DISTINCT valor, ', ') INTO v_invalidos
    FROM (
        SELECT ano_lectivo::text AS valor FROM cursos
        UNION ALL SELECT ano_lectivo::text FROM curso_materia
        UNION ALL SELECT ano_lectivo::text FROM curso_estudiante
        UNION ALL SELECT ano_lectivo::text FROM matriculas
    ) t
    WHERE valor !~ '^[0-9]{4}$';
    IF v_invalidos IS NOT NULL THEN
        RAISE EXCEPTION 'V18: ano_lectivo con formato invalido: %', v_invalidos;
    END IF;
END $$;

-- 2. Crear los años que falten dinámicamente (estado PLANIFICACION, es_actual = false)
INSERT INTO anios_lectivos (id, anio, estado, es_actual)
SELECT gen_random_uuid(), y.anio, 'PLANIFICACION', false
FROM (
    SELECT DISTINCT CAST(s.ano_lectivo AS INTEGER) AS anio
    FROM (
        SELECT ano_lectivo FROM cursos
        UNION SELECT ano_lectivo FROM curso_materia
        UNION SELECT ano_lectivo FROM curso_estudiante
        UNION SELECT ano_lectivo FROM matriculas
    ) s
) y
WHERE NOT EXISTS (SELECT 1 FROM anios_lectivos a WHERE a.anio = y.anio);

-- 3. Sanitizar matriculas no aprobadas cuyo curso no coincide con su año
UPDATE matriculas m
SET curso_id = NULL
FROM cursos c
WHERE m.curso_id = c.id
  AND CAST(m.ano_lectivo AS INTEGER) <> CAST(c.ano_lectivo AS INTEGER)
  AND m.estado <> 'APROBADA';

-- 4. Columnas nuevas de clave foránea hacia anios_lectivos (inicialmente nullable)
ALTER TABLE cursos            ADD COLUMN anio_lectivo_id UUID REFERENCES anios_lectivos(id);
ALTER TABLE curso_materia     ADD COLUMN anio_lectivo_id UUID REFERENCES anios_lectivos(id);
ALTER TABLE curso_estudiante  ADD COLUMN anio_lectivo_id UUID REFERENCES anios_lectivos(id);
ALTER TABLE matriculas        ADD COLUMN anio_lectivo_id UUID REFERENCES anios_lectivos(id);

-- 5. Backfill de anio_lectivo_id a partir de anios_lectivos.anio
UPDATE cursos t           SET anio_lectivo_id = a.id FROM anios_lectivos a WHERE a.anio = CAST(t.ano_lectivo AS INTEGER);
UPDATE curso_materia t    SET anio_lectivo_id = a.id FROM anios_lectivos a WHERE a.anio = CAST(t.ano_lectivo AS INTEGER);
UPDATE curso_estudiante t SET anio_lectivo_id = a.id FROM anios_lectivos a WHERE a.anio = CAST(t.ano_lectivo AS INTEGER);
UPDATE matriculas t       SET anio_lectivo_id = a.id FROM anios_lectivos a WHERE a.anio = CAST(t.ano_lectivo AS INTEGER);

-- 6. Verificar que no quedan nulos ni hijos con año distinto al de su curso
DO $$
DECLARE v_nulos BIGINT; v_desfases BIGINT;
BEGIN
    SELECT (SELECT count(*) FROM cursos           WHERE anio_lectivo_id IS NULL)
         + (SELECT count(*) FROM curso_materia    WHERE anio_lectivo_id IS NULL)
         + (SELECT count(*) FROM curso_estudiante WHERE anio_lectivo_id IS NULL)
         + (SELECT count(*) FROM matriculas       WHERE anio_lectivo_id IS NULL)
    INTO v_nulos;
    IF v_nulos > 0 THEN
        RAISE EXCEPTION 'V18: % filas sin anio_lectivo_id tras el backfill', v_nulos;
    END IF;

    SELECT (SELECT count(*) FROM curso_materia cm    JOIN cursos c ON c.id = cm.curso_id WHERE cm.anio_lectivo_id <> c.anio_lectivo_id)
         + (SELECT count(*) FROM curso_estudiante ce JOIN cursos c ON c.id = ce.curso_id WHERE ce.anio_lectivo_id <> c.anio_lectivo_id)
         + (SELECT count(*) FROM matriculas m        JOIN cursos c ON c.id = m.curso_id  WHERE m.anio_lectivo_id <> c.anio_lectivo_id)
    INTO v_desfases;
    IF v_desfases > 0 THEN
        RAISE EXCEPTION 'V18: % filas con año distinto al de su curso', v_desfases;
    END IF;
END $$;

-- 7. Restricción NOT NULL obligatoria
ALTER TABLE cursos           ALTER COLUMN anio_lectivo_id SET NOT NULL;
ALTER TABLE curso_materia    ALTER COLUMN anio_lectivo_id SET NOT NULL;
ALTER TABLE curso_estudiante ALTER COLUMN anio_lectivo_id SET NOT NULL;
ALTER TABLE matriculas       ALTER COLUMN anio_lectivo_id SET NOT NULL;

-- 8. Unicidades nuevas (crear primero, luego eliminar las basadas en texto)
ALTER TABLE cursos ADD CONSTRAINT uk_curso_grado_grupo_anio_lectivo UNIQUE (grado, grupo, anio_lectivo_id);
ALTER TABLE cursos ADD CONSTRAINT uk_cursos_id_anio_lectivo UNIQUE (id, anio_lectivo_id);
ALTER TABLE curso_materia ADD CONSTRAINT uk_curso_materia_anio_lectivo UNIQUE (curso_id, materia_id, anio_lectivo_id);
ALTER TABLE curso_estudiante ADD CONSTRAINT uk_curso_estudiante_anio_lectivo UNIQUE (estudiante_id, anio_lectivo_id);
ALTER TABLE matriculas ADD CONSTRAINT uk_matriculas_estudiante_anio_lectivo UNIQUE (estudiante_id, anio_lectivo_id);

ALTER TABLE cursos           DROP CONSTRAINT uk_curso_grado_grupo_ano;
ALTER TABLE curso_materia    DROP CONSTRAINT uk_curso_materia_ano;
ALTER TABLE curso_estudiante DROP CONSTRAINT uk_curso_estudiante_ano;

-- 9. Claves Foráneas compuestas para blindar contra desfases de año entre curso e hijos
ALTER TABLE curso_materia    ADD CONSTRAINT fk_cm_curso_anio
    FOREIGN KEY (curso_id, anio_lectivo_id) REFERENCES cursos (id, anio_lectivo_id);
ALTER TABLE curso_estudiante ADD CONSTRAINT fk_ce_curso_anio
    FOREIGN KEY (curso_id, anio_lectivo_id) REFERENCES cursos (id, anio_lectivo_id);
ALTER TABLE matriculas       ADD CONSTRAINT fk_mat_curso_anio
    FOREIGN KEY (curso_id, anio_lectivo_id) REFERENCES cursos (id, anio_lectivo_id);

-- 10. Índices de rendimiento
CREATE INDEX idx_curso_estudiante_curso ON curso_estudiante (curso_id);
CREATE INDEX idx_cursos_anio            ON cursos (anio_lectivo_id);
CREATE INDEX idx_curso_materia_anio     ON curso_materia (anio_lectivo_id);
CREATE INDEX idx_matriculas_anio        ON matriculas (anio_lectivo_id);
