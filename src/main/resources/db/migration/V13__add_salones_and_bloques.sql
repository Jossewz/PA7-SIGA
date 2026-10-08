-- =======================================================
-- FLYWAY MIGRATION V13: Salones Fisicos y Bloques Horarios Catalogados
-- =======================================================

-- 1. Tabla: salones
CREATE TABLE IF NOT EXISTS salones (
    id UUID PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    capacidad INT DEFAULT 35,
    edificio VARCHAR(100) DEFAULT 'Bloque Principal',
    tipo VARCHAR(50) DEFAULT 'AULA',
    estado VARCHAR(50) DEFAULT 'Activo',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- 2. Tabla: bloques
CREATE TABLE IF NOT EXISTS bloques (
    id UUID PRIMARY KEY,
    numero INT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    jornada VARCHAR(50) DEFAULT 'Mañana',
    tipo VARCHAR(50) DEFAULT 'CLASE',
    estado VARCHAR(50) DEFAULT 'Activo',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_bloque_numero_jornada UNIQUE(numero, jornada)
);

-- 3. Vincular columnas FK en la tabla horarios
ALTER TABLE horarios 
    ADD COLUMN IF NOT EXISTS salon_id UUID REFERENCES salones(id);

ALTER TABLE horarios 
    ADD COLUMN IF NOT EXISTS bloque_id UUID REFERENCES bloques(id);

-- 4. Semilla de Salones Oficiales
INSERT INTO salones (id, codigo, nombre, capacidad, edificio, tipo, estado) VALUES
    ('b0000000-0000-0000-0000-000000000101', 'AULA-101', 'Aula 101 - Grado 11', 40, 'Bloque A - Piso 1', 'AULA', 'Activo'),
    ('b0000000-0000-0000-0000-000000000102', 'AULA-102', 'Aula 102 - Grado 10', 35, 'Bloque A - Piso 1', 'AULA', 'Activo'),
    ('b0000000-0000-0000-0000-000000000201', 'AULA-201', 'Aula 201 - Grado 9', 35, 'Bloque A - Piso 2', 'AULA', 'Activo'),
    ('b0000000-0000-0000-0000-000000000202', 'AULA-202', 'Aula 202 - Grado 8', 35, 'Bloque A - Piso 2', 'AULA', 'Activo'),
    ('b0000000-0000-0000-0000-000000000301', 'LAB-CIENCIAS', 'Laboratorio de Ciencias Integradas', 30, 'Bloque B - Laboratorios', 'LABORATORIO', 'Activo'),
    ('b0000000-0000-0000-0000-000000000302', 'SALA-SISTEMAS', 'Sala de Cómputo y Robótica', 35, 'Bloque B - Tecnología', 'SALA_SISTEMAS', 'Activo')
ON CONFLICT (codigo) DO NOTHING;

-- 5. Semilla de Bloques Horarios (Jornada Mañana)
INSERT INTO bloques (id, numero, nombre, hora_inicio, hora_fin, jornada, tipo, estado) VALUES
    ('c0000000-0000-0000-0000-000000000001', 1, 'Bloque 1', '07:00:00', '08:30:00', 'Mañana', 'CLASE', 'Activo'),
    ('c0000000-0000-0000-0000-000000000002', 2, 'Bloque 2', '08:30:00', '10:00:00', 'Mañana', 'CLASE', 'Activo'),
    ('c0000000-0000-0000-0000-000000000003', 3, 'Descanso Institucional', '10:00:00', '10:30:00', 'Mañana', 'DESCANSO', 'Activo'),
    ('c0000000-0000-0000-0000-000000000004', 4, 'Bloque 3', '10:30:00', '12:00:00', 'Mañana', 'CLASE', 'Activo'),
    ('c0000000-0000-0000-0000-000000000005', 5, 'Bloque 4', '12:00:00', '13:30:00', 'Mañana', 'CLASE', 'Activo')
ON CONFLICT (numero, jornada) DO NOTHING;

-- 6. Backfill para horarios existentes
UPDATE horarios 
SET salon_id = 'b0000000-0000-0000-0000-000000000101'
WHERE salon_id IS NULL AND (salon ILIKE '%101%' OR salon IS NULL);

UPDATE horarios 
SET bloque_id = 'c0000000-0000-0000-0000-000000000001'
WHERE bloque_id IS NULL AND hora_inicio = '07:00:00';
