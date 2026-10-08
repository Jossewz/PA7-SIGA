-- V21: Armonización con el Sistema Institucional de Evaluación de los Estudiantes (SIEACI Oficial GC-F05)
-- Institución Educativa Ambientalista de Cartagena de Indias

-- 1. Ponderación oficial de periodos académicos (SIEACI Ítem 7.1):
--    - Periodo 1 (13 semanas): 33%
--    - Periodo 2 (12 semanas): 33%
--    - Periodo 3 (15 semanas): 34%
UPDATE periodos_academicos SET peso_porcentaje = 33.00 WHERE numero_periodo = 1;
UPDATE periodos_academicos SET peso_porcentaje = 33.00 WHERE numero_periodo = 2;
UPDATE periodos_academicos SET peso_porcentaje = 34.00 WHERE numero_periodo = 3;

-- 2. Escala valorativa institucional oficial (SIEACI Ítem 5.4.11 / Decreto 1290 de 2009):
--    Se suprimen niveles no contemplados en la escala nacional ('CRITICO' y 'MUY_BAJO')
--    y se homologan los 4 desempeños oficiales en el rango normativo de 1.00 a 5.00:
DELETE FROM escala_desempeno WHERE codigo IN ('CRITICO', 'MUY_BAJO');

UPDATE escala_desempeno 
SET nota_minima = 1.00, nota_maxima = 2.90, nombre = 'Desempeño Bajo', orden = 4
WHERE codigo = 'BAJO';

UPDATE escala_desempeno 
SET nota_minima = 3.00, nota_maxima = 3.90, nombre = 'Desempeño Básico', orden = 3
WHERE codigo = 'BASICO';

UPDATE escala_desempeno 
SET nota_minima = 4.00, nota_maxima = 4.50, nombre = 'Desempeño Alto', orden = 2
WHERE codigo = 'ALTO';

UPDATE escala_desempeno 
SET nota_minima = 4.60, nota_maxima = 5.00, nombre = 'Desempeño Superior', orden = 1
WHERE codigo = 'SUPERIOR';
