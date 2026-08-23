DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_documento_owner'
    ) THEN
        ALTER TABLE documentos
        ADD CONSTRAINT chk_documento_owner
        CHECK (
            (
                (matricula_id IS NOT NULL)::int +
                (estudiante_id IS NOT NULL)::int +
                (docente_id IS NOT NULL)::int +
                (personal_id IS NOT NULL)::int
            ) = 1
        );
    END IF;
END $$;
