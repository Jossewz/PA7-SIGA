-- =======================================================
-- FLYWAY MIGRATION V1: Initial Schema SIGA IEACI
-- =======================================================

-- 1. Tabla: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    rol VARCHAR(100) NOT NULL,
    numero_documento VARCHAR(100) NOT NULL UNIQUE,
    estado VARCHAR(50) DEFAULT 'Activo',
    ultimo_acceso TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

-- 2. Tabla: acudientes
CREATE TABLE IF NOT EXISTS acudientes (
    id UUID PRIMARY KEY,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    parentesco VARCHAR(100),
    tipo_documento VARCHAR(50),
    numero_documento VARCHAR(100) UNIQUE,
    telefono VARCHAR(50),
    direccion VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE
);

-- 3. Tabla: docentes
CREATE TABLE IF NOT EXISTS docentes (
    id UUID PRIMARY KEY,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50),
    numero_documento VARCHAR(100) UNIQUE,
    genero VARCHAR(50),
    telefono VARCHAR(50),
    fecha_nacimiento DATE,
    direccion VARCHAR(255),
    especialidad VARCHAR(150),
    titulo VARCHAR(150),
    estado VARCHAR(50) DEFAULT 'Activo',
    foto_key VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- 4. Tabla: personal_administrativo
CREATE TABLE IF NOT EXISTS personal_administrativo (
    id UUID PRIMARY KEY,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50),
    numero_documento VARCHAR(100) UNIQUE,
    cargo VARCHAR(150),
    area VARCHAR(150),
    genero VARCHAR(50),
    telefono VARCHAR(50),
    fecha_nacimiento DATE,
    direccion VARCHAR(255),
    estado VARCHAR(50) DEFAULT 'Activo',
    foto_key VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- 5. Tabla: estudiantes
CREATE TABLE IF NOT EXISTS estudiantes (
    id UUID PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50),
    numero_documento VARCHAR(100) UNIQUE,
    genero VARCHAR(50),
    telefono VARCHAR(50),
    fecha_nacimiento DATE,
    direccion VARCHAR(255),
    estado VARCHAR(50) DEFAULT 'Activo',
    foto_key VARCHAR(255),
    acudiente_id UUID REFERENCES acudientes(id),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- 6. Tabla: matriculas
CREATE TABLE IF NOT EXISTS matriculas (
    id UUID PRIMARY KEY,
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id),
    grado VARCHAR(100) NOT NULL,
    salon VARCHAR(50) DEFAULT '01',
    ano_lectivo VARCHAR(50) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    fecha_matricula DATE
);

-- 7. Tabla: documentos
CREATE TABLE IF NOT EXISTS documentos (
    id UUID PRIMARY KEY,
    storage_key VARCHAR(500) NOT NULL,
    nombre_original VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    size BIGINT,
    tipo_documento VARCHAR(100) NOT NULL,
    checksum VARCHAR(255),
    matricula_id UUID REFERENCES matriculas(id),
    estudiante_id UUID REFERENCES estudiantes(id),
    docente_id UUID REFERENCES docentes(id),
    personal_id UUID REFERENCES personal_administrativo(id),
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT chk_documento_owner CHECK (
        (
            (matricula_id IS NOT NULL)::int +
            (estudiante_id IS NOT NULL)::int +
            (docente_id IS NOT NULL)::int +
            (personal_id IS NOT NULL)::int
        ) = 1
    )
);
