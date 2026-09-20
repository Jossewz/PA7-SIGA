-- =======================================================
-- FLYWAY MIGRATION V10: Actualizacion de Descripciones y Permisos Dinamicos RBAC
-- =======================================================

-- Corregir descripciones que decian 'No autorizado' para que describan la funcionalidad del modulo
UPDATE rol_permisos SET descripcion = 'Gestión y consulta de matrículas y expedientes académicos' WHERE modulo = 'MATRICULAS';
UPDATE rol_permisos SET descripcion = 'Directorio institucional de docentes y personal administrativo' WHERE modulo = 'PERSONAL';
UPDATE rol_permisos SET descripcion = 'Gestión y consulta de cursos, asignaturas y horarios' WHERE modulo = 'CURSOS_HORARIOS';
UPDATE rol_permisos SET descripcion = 'Consulta y registro de calificaciones, evaluaciones y boletines' WHERE modulo = 'CALIFICACIONES';
UPDATE rol_permisos SET descripcion = 'Control y registro de asistencia a clases' WHERE modulo = 'ASISTENCIAS';
UPDATE rol_permisos SET descripcion = 'Solicitud y expedición de certificados de estudio y constancias' WHERE modulo = 'CERTIFICADOS';
UPDATE rol_permisos SET descripcion = 'Generación de reportes institucionales y analítica académica' WHERE modulo = 'REPORTES';
UPDATE rol_permisos SET descripcion = 'Parámetros institucionales, periodos, escala de notas y permisos' WHERE modulo = 'CONFIGURACION';
UPDATE rol_permisos SET descripcion = 'Proyectos ecológicos y evidencias ambientales PRAE' WHERE modulo = 'MODULO_AMBIENTAL';
