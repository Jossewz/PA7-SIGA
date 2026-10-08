SET client_encoding = 'UTF8';

UPDATE docentes 
SET apellidos = 'García' 
WHERE id = 'd0000000-0000-0000-0000-000000000002';

UPDATE materias 
SET nombre = 'Matemáticas' 
WHERE id = 'ba000000-0000-0000-0000-000000000001';

UPDATE cursos 
SET jornada = 'Mañana' 
WHERE jornada LIKE 'Ma%ana';
