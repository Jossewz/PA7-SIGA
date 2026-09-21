-- =======================================================
-- FLYWAY MIGRATION V9: Parametros del Sistema, Escala de Desempeno y Roles/Permisos
-- =======================================================

-- 1. Tabla: configuracion_institucional
CREATE TABLE IF NOT EXISTS configuracion_institucional (
    id UUID PRIMARY KEY,
    nit VARCHAR(50) NOT NULL,
    nombre_inst VARCHAR(255) NOT NULL,
    direccion_inst VARCHAR(255),
    telefono_inst VARCHAR(50),
    correo_inst VARCHAR(150),
    ano_lectivo VARCHAR(10) NOT NULL DEFAULT '2026',
    rector_nombre VARCHAR(150),
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

INSERT INTO configuracion_institucional (id, nit, nombre_inst, direccion_inst, telefono_inst, correo_inst, ano_lectivo, rector_nombre, updated_at)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    '800.124.567-2',
    'Institución Educativa Ateneo de la Ciencia e Innovación (IEACI)',
    'San José de los Campanos, Mz 32 Lote 9-11',
    '300 987 6543',
    'contacto@ieaci.edu.co',
    '2026',
    'Rectoría General',
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- 2. Tabla: periodos_academicos
CREATE TABLE IF NOT EXISTS periodos_academicos (
    id UUID PRIMARY KEY,
    numero_periodo INT NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    peso_porcentaje DECIMAL(5,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(50) DEFAULT 'Activo'
);

INSERT INTO periodos_academicos (id, numero_periodo, nombre, peso_porcentaje, fecha_inicio, fecha_fin, estado)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 1, 'Primer Período', 30.00, '2026-02-01', '2026-06-15', 'Activo'),
    ('22222222-2222-2222-2222-222222222222', 2, 'Segundo Período', 35.00, '2026-07-15', '2026-09-15', 'Activo'),
    ('33333333-3333-3333-3333-333333333333', 3, 'Tercer Período', 35.00, '2026-09-16', '2026-11-30', 'Activo')
ON CONFLICT (numero_periodo) DO NOTHING;

-- 3. Tabla: escala_desempeno
CREATE TABLE IF NOT EXISTS escala_desempeno (
    id UUID PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    nota_minima DECIMAL(3,2) NOT NULL,
    nota_maxima DECIMAL(3,2) NOT NULL,
    color_badge VARCHAR(50) NOT NULL,
    color_hex VARCHAR(20) NOT NULL,
    orden INT NOT NULL
);

INSERT INTO escala_desempeno (id, codigo, nombre, nota_minima, nota_maxima, color_badge, color_hex, orden)
VALUES
    ('aaaaaaaa-1111-0000-0000-000000000001', 'SUPERIOR', 'Desempeño Superior', 4.60, 5.00, 'bg-sidebar text-white', '#1b5e20', 1),
    ('aaaaaaaa-1111-0000-0000-000000000002', 'ALTO', 'Desempeño Alto', 4.00, 4.59, 'bg-emerald-600 text-white', '#059669', 2),
    ('aaaaaaaa-1111-0000-0000-000000000003', 'BASICO', 'Desempeño Básico', 3.00, 3.99, 'bg-amber-500 text-white', '#f59e0b', 3),
    ('aaaaaaaa-1111-0000-0000-000000000004', 'BAJO', 'Desempeño Bajo', 2.00, 2.99, 'bg-orange-500 text-white', '#f97316', 4),
    ('aaaaaaaa-1111-0000-0000-000000000005', 'MUY_BAJO', 'Desempeño Muy Bajo', 1.00, 1.99, 'bg-rose-500 text-white', '#f43f5e', 5),
    ('aaaaaaaa-1111-0000-0000-000000000006', 'CRITICO', 'Desempeño Crítico', 0.00, 0.99, 'bg-red-700 text-white', '#b91c1c', 6)
ON CONFLICT (codigo) DO NOTHING;

-- 4. Tabla: rol_permisos
CREATE TABLE IF NOT EXISTS rol_permisos (
    id UUID PRIMARY KEY,
    rol VARCHAR(100) NOT NULL,
    modulo VARCHAR(100) NOT NULL,
    nombre_modulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    puede_acceder BOOLEAN DEFAULT true,
    puede_editar BOOLEAN DEFAULT true,
    puede_eliminar BOOLEAN DEFAULT false,
    CONSTRAINT uk_rol_modulo UNIQUE(rol, modulo)
);

-- Permisos iniciales para ADMIN
INSERT INTO rol_permisos (id, rol, modulo, nombre_modulo, descripcion, puede_acceder, puede_editar, puede_eliminar) VALUES
    (gen_random_uuid(), 'ADMIN', 'MATRICULAS', 'Matrículas y Estudiantes', 'Gestión integral de matrículas y expedientes académicos', true, true, true),
    (gen_random_uuid(), 'ADMIN', 'PERSONAL', 'Directorio de Personal', 'Gestión de docentes, directivos y administrativos', true, true, true),
    (gen_random_uuid(), 'ADMIN', 'CURSOS_HORARIOS', 'Cursos y Horarios', 'Asignación de asignaturas, horarios y mapeo de estudiantes', true, true, true),
    (gen_random_uuid(), 'ADMIN', 'CALIFICACIONES', 'Calificaciones y Boletines', 'Control institucional de notas y promedios definitivos', true, true, true),
    (gen_random_uuid(), 'ADMIN', 'ASISTENCIAS', 'Control de Asistencias', 'Registro y supervisión de asistencia por curso', true, true, true),
    (gen_random_uuid(), 'ADMIN', 'CERTIFICADOS', 'Certificados y Constancias', 'Generación y expedición de documentos oficiales', true, true, true),
    (gen_random_uuid(), 'ADMIN', 'REPORTES', 'Reportes y Estadísticas', 'Consultas globales, citaciones y analítica', true, true, true),
    (gen_random_uuid(), 'ADMIN', 'CONFIGURACION', 'Configuración del Sistema', 'Parámetros institucionales, periodos, notas y permisos', true, true, true),
    (gen_random_uuid(), 'ADMIN', 'MODULO_AMBIENTAL', 'Módulo Ambiental PRAE', 'Proyectos ambientales y evidencias ecológicas', true, true, true)
ON CONFLICT (rol, modulo) DO NOTHING;

-- Permisos iniciales para PERSONAL_ADMINISTRATIVO
INSERT INTO rol_permisos (id, rol, modulo, nombre_modulo, descripcion, puede_acceder, puede_editar, puede_eliminar) VALUES
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'MATRICULAS', 'Matrículas y Estudiantes', 'Gestión integral de matrículas y expedientes académicos', true, true, false),
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'PERSONAL', 'Directorio de Personal', 'Consulta y actualización básica del personal', true, true, false),
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'CURSOS_HORARIOS', 'Cursos y Horarios', 'Mapeo y supervisión de cursos', true, true, false),
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'CALIFICACIONES', 'Calificaciones y Boletines', 'Consulta y generación de boletines', true, false, false),
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'ASISTENCIAS', 'Control de Asistencias', 'Supervisión de asistencias', true, false, false),
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'CERTIFICADOS', 'Certificados y Constancias', 'Generación de certificados oficiales', true, true, false),
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'REPORTES', 'Reportes y Estadísticas', 'Generación de reportes institucionales', true, true, false),
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'CONFIGURACION', 'Configuración del Sistema', 'Consulta de parámetros del sistema', true, false, false),
    (gen_random_uuid(), 'PERSONAL_ADMINISTRATIVO', 'MODULO_AMBIENTAL', 'Módulo Ambiental PRAE', 'Registro y consulta ambiental', true, true, false)
ON CONFLICT (rol, modulo) DO NOTHING;

-- Permisos iniciales para DOCENTE
INSERT INTO rol_permisos (id, rol, modulo, nombre_modulo, descripcion, puede_acceder, puede_editar, puede_eliminar) VALUES
    (gen_random_uuid(), 'DOCENTE', 'MATRICULAS', 'Matrículas y Estudiantes', 'Consulta de estudiantes asignados', true, false, false),
    (gen_random_uuid(), 'DOCENTE', 'PERSONAL', 'Directorio de Personal', 'Directorio institucional de contactos', false, false, false),
    (gen_random_uuid(), 'DOCENTE', 'CURSOS_HORARIOS', 'Cursos y Horarios', 'Visualización de cursos y horarios propios asignados', true, false, false),
    (gen_random_uuid(), 'DOCENTE', 'CALIFICACIONES', 'Calificaciones y Evaluaciones', 'Ingreso de notas y ponderación de actividades en sus clases', true, true, false),
    (gen_random_uuid(), 'DOCENTE', 'ASISTENCIAS', 'Control de Asistencias', 'Toma diaria de asistencia en sus asignaturas', true, true, false),
    (gen_random_uuid(), 'DOCENTE', 'CERTIFICADOS', 'Certificados y Constancias', 'No autorizado', false, false, false),
    (gen_random_uuid(), 'DOCENTE', 'REPORTES', 'Reportes y Estadísticas', 'Reportes académicos de sus asignaturas', true, false, false),
    (gen_random_uuid(), 'DOCENTE', 'CONFIGURACION', 'Configuración del Sistema', 'No autorizado', false, false, false),
    (gen_random_uuid(), 'DOCENTE', 'MODULO_AMBIENTAL', 'Módulo Ambiental PRAE', 'Participación en proyectos ecológicos', true, true, false)
ON CONFLICT (rol, modulo) DO NOTHING;

-- Permisos iniciales para ESTUDIANTE
INSERT INTO rol_permisos (id, rol, modulo, nombre_modulo, descripcion, puede_acceder, puede_editar, puede_eliminar) VALUES
    (gen_random_uuid(), 'ESTUDIANTE', 'MATRICULAS', 'Matrículas y Estudiantes', 'Consulta de su propio expediente', true, false, false),
    (gen_random_uuid(), 'ESTUDIANTE', 'PERSONAL', 'Directorio de Personal', 'No autorizado', false, false, false),
    (gen_random_uuid(), 'ESTUDIANTE', 'CURSOS_HORARIOS', 'Cursos y Horarios', 'Consulta de su horario de clases', true, false, false),
    (gen_random_uuid(), 'ESTUDIANTE', 'CALIFICACIONES', 'Calificaciones y Boletines', 'Consulta de su boletín de calificaciones', true, false, false),
    (gen_random_uuid(), 'ESTUDIANTE', 'ASISTENCIAS', 'Control de Asistencias', 'Consulta de su registro de asistencia', true, false, false),
    (gen_random_uuid(), 'ESTUDIANTE', 'CERTIFICADOS', 'Certificados y Constancias', 'Solicitud y descarga de certificados', true, false, false),
    (gen_random_uuid(), 'ESTUDIANTE', 'REPORTES', 'Reportes y Estadísticas', 'No autorizado', false, false, false),
    (gen_random_uuid(), 'ESTUDIANTE', 'CONFIGURACION', 'Configuración del Sistema', 'No autorizado', false, false, false),
    (gen_random_uuid(), 'ESTUDIANTE', 'MODULO_AMBIENTAL', 'Módulo Ambiental PRAE', 'Consulta de iniciativas ambientales', true, false, false)
ON CONFLICT (rol, modulo) DO NOTHING;
