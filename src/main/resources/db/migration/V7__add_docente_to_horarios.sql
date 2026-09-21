-- Migration V7: Add docente_id foreign key column to horarios table
ALTER TABLE horarios ADD COLUMN IF NOT EXISTS docente_id UUID REFERENCES docentes(id) ON DELETE SET NULL;
