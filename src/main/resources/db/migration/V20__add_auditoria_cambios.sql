-- V20__add_auditoria_cambios.sql
-- Tabla inmutable para trazabilidad de modificaciones en calificaciones, asistencias y datos sensibles.

CREATE TABLE auditoria_cambios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad VARCHAR(50) NOT NULL,
    entidad_id UUID NOT NULL,
    campo VARCHAR(50) NOT NULL,
    valor_anterior TEXT,
    valor_nuevo TEXT,
    usuario_id UUID,
    usuario_email VARCHAR(100),
    fecha TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auditoria_cambios_entidad_id ON auditoria_cambios(entidad, entidad_id);
CREATE INDEX idx_auditoria_cambios_fecha ON auditoria_cambios(fecha);

-- Regla de integridad física: la tabla es de solo inserción (append-only).
-- Se bloquea cualquier mutación o borrado de registros de auditoría mediante trigger.
CREATE OR REPLACE FUNCTION trg_bloquear_modificacion_auditoria()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'La tabla auditoria_cambios es de solo insercion (append-only). Operaciones UPDATE o DELETE no estan permitidas.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_auditoria_cambios_bloquear_modificacion ON auditoria_cambios;
CREATE TRIGGER trg_auditoria_cambios_bloquear_modificacion
BEFORE UPDATE OR DELETE ON auditoria_cambios
FOR EACH ROW EXECUTE FUNCTION trg_bloquear_modificacion_auditoria();

