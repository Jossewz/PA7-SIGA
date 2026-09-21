-- =======================================================
-- FLYWAY MIGRATION V3: Cursos, Materias y Horarios
-- =======================================================

CREATE TABLE IF NOT EXISTS cursos (
    id UUID PRIMARY KEY,
    grado VARCHAR(50) NOT NULL,
    grupo VARCHAR(10) NOT NULL DEFAULT '01',
    jornada VARCHAR(50) DEFAULT 'Mañana',
    cupos_maximos INT DEFAULT 35,
    director_id UUID REFERENCES docentes(id),
    ano_lectivo VARCHAR(10) NOT NULL,
    estado VARCHAR(50) DEFAULT 'Activo',
    created_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_curso_grado_grupo_ano UNIQUE(grado, grupo, ano_lectivo)
);

CREATE TABLE IF NOT EXISTS materias (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    area VARCHAR(150),
    intensidad_horaria INT DEFAULT 4,
    estado VARCHAR(50) DEFAULT 'Activo'
);

CREATE TABLE IF NOT EXISTS curso_materia (
    id UUID PRIMARY KEY,
    curso_id UUID NOT NULL REFERENCES cursos(id),
    materia_id UUID NOT NULL REFERENCES materias(id),
    docente_id UUID REFERENCES docentes(id),
    ano_lectivo VARCHAR(10) NOT NULL,
    CONSTRAINT uk_curso_materia_ano UNIQUE(curso_id, materia_id, ano_lectivo)
);

CREATE TABLE IF NOT EXISTS horarios (
    id UUID PRIMARY KEY,
    curso_id UUID NOT NULL REFERENCES cursos(id),
    materia_id UUID REFERENCES materias(id),
    dia_semana VARCHAR(20) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    salon VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS curso_estudiante (
    id UUID PRIMARY KEY,
    curso_id UUID NOT NULL REFERENCES cursos(id),
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id),
    ano_lectivo VARCHAR(10) NOT NULL,
    CONSTRAINT uk_curso_estudiante_ano UNIQUE(curso_id, estudiante_id, ano_lectivo)
);
