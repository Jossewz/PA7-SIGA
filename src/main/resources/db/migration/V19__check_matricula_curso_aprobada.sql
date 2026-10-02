-- V19__check_matricula_curso_aprobada.sql
-- Restricción condicional: Una matrícula APROBADA debe contar obligatoriamente con un curso asignado.
-- Matrículas en trámite o borrador (ej. PENDIENTE_DE_REVISION) pueden tener curso_id NULL.

DO $$
DECLARE v BIGINT;
BEGIN
    SELECT count(*) INTO v FROM matriculas WHERE estado = 'APROBADA' AND curso_id IS NULL;
    IF v > 0 THEN
        RAISE EXCEPTION 'V19: % matriculas APROBADA sin curso_id', v;
    END IF;
END $$;

ALTER TABLE matriculas
    ADD CONSTRAINT chk_matricula_curso_aprobada
    CHECK (estado <> 'APROBADA' OR curso_id IS NOT NULL);
