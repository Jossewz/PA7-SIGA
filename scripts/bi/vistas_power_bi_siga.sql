-- ==============================================================================
-- CAPA ANALÍTICA PARA POWER BI: MODELO ESTRELLA SIGA - IEACI
-- Vistas dimensionales y de hechos para el Coordinador Académico
-- Compatible con PostgreSQL 18+ y Power BI Desktop
-- ==============================================================================

-- 1. DIMENSIÓN CURSO
CREATE OR REPLACE VIEW vw_dim_curso AS
SELECT 
    c.id AS curso_id,
    c.nombre AS curso_nombre,
    c.grado AS grado_nombre,
    c.grupo AS grupo_nombre,
    c.jornada AS jornada_nombre,
    c.nivel_educativo,
    al.id AS anio_lectivo_id,
    al.anio AS anio_lectivo,
    al.es_actual AS anio_es_actual,
    COALESCE(u.nombres || ' ' || u.apellidos, 'Sin Asignar') AS director_grupo_nombre,
    CASE 
        WHEN c.grado ILIKE '%transici%' THEN 0
        WHEN c.grado ~ '^[0-9]+' THEN CAST(SUBSTRING(c.grado FROM '^[0-9]+') AS INTEGER)
        ELSE 99
    END AS grado_numero
FROM cursos c
INNER JOIN anios_lectivos al ON c.anio_lectivo_id = al.id
LEFT JOIN docentes d ON c.director_grupo_id = d.id
LEFT JOIN usuarios u ON d.usuario_id = u.id;

-- 2. DIMENSIÓN ESTUDIANTE
CREATE OR REPLACE VIEW vw_dim_estudiante AS
SELECT 
    e.id AS estudiante_id,
    e.codigo_estudiantil,
    e.tipo_documento,
    e.numero_documento,
    e.nombres || ' ' || e.apellidos AS estudiante_nombre_completo,
    e.nombres,
    e.apellidos,
    e.genero,
    e.fecha_nacimiento,
    e.estado AS estado_academico,
    e.creado_en AS fecha_registro
FROM estudiantes e;

-- 3. DIMENSIÓN MATERIA / ASIGNATURA
CREATE OR REPLACE VIEW vw_dim_materia AS
SELECT 
    m.id AS materia_id,
    m.nombre AS materia_nombre,
    m.codigo AS materia_codigo,
    m.area AS area_fundamental,
    m.intensidad_horaria,
    m.porcentaje_area
FROM materias m;

-- 4. DIMENSIÓN ESTADO DE REPORTE
CREATE OR REPLACE VIEW vw_dim_estado_reporte AS
SELECT 
    estado_codigo,
    estado_etiqueta,
    orden_flujo,
    color_hex
FROM (
    VALUES 
        ('RADICADO', 'Radicado (Creado)', 1, '#FFC107'),
        ('EN_REVISION', 'En Revisión (Coordinación)', 2, '#0D6EFD'),
        ('CONTESTADO', 'Contestado (Cerrado)', 3, '#198754')
) AS t(estado_codigo, estado_etiqueta, orden_flujo, color_hex);

-- 5. DIMENSIÓN TIEMPO INSTITUCIONAL
CREATE OR REPLACE VIEW vw_dim_tiempo AS
WITH limites AS (
    SELECT 
        COALESCE(MIN(fecha_inicio), '2026-01-01'::date) AS f_inicio,
        COALESCE(MAX(fecha_fin), '2026-12-31'::date) AS f_fin
    FROM periodos_academicos
),
calendario AS (
    SELECT generate_series(f_inicio, f_fin, '1 day'::interval)::date AS fecha
    FROM limites
)
SELECT 
    c.fecha,
    EXTRACT(YEAR FROM c.fecha)::int AS anio,
    EXTRACT(MONTH FROM c.fecha)::int AS mes,
    TO_CHAR(c.fecha, 'TMMonth') AS mes_nombre,
    EXTRACT(DAY FROM c.fecha)::int AS dia,
    EXTRACT(ISODOW FROM c.fecha)::int AS dia_semana_iso,
    CASE EXTRACT(ISODOW FROM c.fecha)::int
        WHEN 1 THEN 'Lunes'
        WHEN 2 THEN 'Martes'
        WHEN 3 THEN 'Miércoles'
        WHEN 4 THEN 'Jueves'
        WHEN 5 THEN 'Viernes'
        WHEN 6 THEN 'Sábado'
        WHEN 7 THEN 'Domingo'
    END AS dia_semana_nombre,
    p.id AS periodo_id,
    COALESCE(p.nombre, 'Interperiodo') AS periodo_nombre,
    COALESCE(p.numero, 0) AS periodo_numero
FROM calendario c
LEFT JOIN periodos_academicos p 
    ON c.fecha >= p.fecha_inicio AND c.fecha <= p.fecha_fin;

-- 6. TABLA DE HECHOS: RENDIMIENTO ACADÉMICO
CREATE OR REPLACE VIEW vw_fact_rendimiento_academico AS
SELECT 
    md5(ce.id::text || '_' || cm.id::text || '_' || p.id::text) AS hecho_id,
    c.id AS curso_id,
    e.id AS estudiante_id,
    m.id AS materia_id,
    p.id AS periodo_id,
    al.id AS anio_lectivo_id,
    COALESCE(
        ROUND(
            (SUM(c_eval.nota * (e_conf.porcentaje / 100.0)) / 
            NULLIF(SUM(CASE WHEN c_eval.nota IS NOT NULL THEN (e_conf.porcentaje / 100.0) ELSE 0 END), 0)
            )::numeric, 2
        ), 0.00
    ) AS calificacion_definitiva,
    CASE 
        WHEN COALESCE(
            ROUND(
                (SUM(c_eval.nota * (e_conf.porcentaje / 100.0)) / 
                NULLIF(SUM(CASE WHEN c_eval.nota IS NOT NULL THEN (e_conf.porcentaje / 100.0) ELSE 0 END), 0)
                )::numeric, 2
            ), 0.00
        ) >= 3.00 THEN 1 ELSE 0 
    END AS es_aprobado,
    CASE 
        WHEN COALESCE(
            ROUND(
                (SUM(c_eval.nota * (e_conf.porcentaje / 100.0)) / 
                NULLIF(SUM(CASE WHEN c_eval.nota IS NOT NULL THEN (e_conf.porcentaje / 100.0) ELSE 0 END), 0)
                )::numeric, 2
            ), 0.00
        ) < 3.00 THEN 1 ELSE 0 
    END AS es_reprobado,
    CASE 
        WHEN COALESCE(
            ROUND(
                (SUM(c_eval.nota * (e_conf.porcentaje / 100.0)) / 
                NULLIF(SUM(CASE WHEN c_eval.nota IS NOT NULL THEN (e_conf.porcentaje / 100.0) ELSE 0 END), 0)
                )::numeric, 2
            ), 0.00
        ) >= 4.60 THEN 'Superior'
        WHEN COALESCE(
            ROUND(
                (SUM(c_eval.nota * (e_conf.porcentaje / 100.0)) / 
                NULLIF(SUM(CASE WHEN c_eval.nota IS NOT NULL THEN (e_conf.porcentaje / 100.0) ELSE 0 END), 0)
                )::numeric, 2
            ), 0.00
        ) >= 4.00 THEN 'Alto'
        WHEN COALESCE(
            ROUND(
                (SUM(c_eval.nota * (e_conf.porcentaje / 100.0)) / 
                NULLIF(SUM(CASE WHEN c_eval.nota IS NOT NULL THEN (e_conf.porcentaje / 100.0) ELSE 0 END), 0)
                )::numeric, 2
            ), 0.00
        ) >= 3.00 THEN 'Básico'
        ELSE 'Bajo'
    END AS escala_desempeno
FROM curso_estudiante ce
INNER JOIN cursos c ON ce.curso_id = c.id
INNER JOIN anios_lectivos al ON c.anio_lectivo_id = al.id
INNER JOIN estudiantes e ON ce.estudiante_id = e.id
INNER JOIN curso_materia cm ON cm.curso_id = c.id
INNER JOIN materias m ON cm.materia_id = m.id
CROSS JOIN periodos_academicos p
LEFT JOIN evaluaciones_config e_conf 
    ON e_conf.curso_materia_id = cm.id AND e_conf.periodo_id = p.id
LEFT JOIN calificaciones c_eval 
    ON c_eval.evaluacion_id = e_conf.id AND c_eval.estudiante_id = e.id
GROUP BY 
    ce.id, c.id, e.id, m.id, p.id, al.id;

-- 7. TABLA DE HECHOS: GESTIÓN DE REPORTES Y CITACIONES
CREATE OR REPLACE VIEW vw_fact_reportes AS
SELECT 
    r.id AS reporte_id,
    r.estudiante_id,
    ce.curso_id,
    r.creado_por AS autor_usuario_id,
    r.tipo AS tipo_reporte,
    r.estado AS estado_codigo,
    r.fecha_incidente::date AS fecha_incidente,
    r.creado_en::date AS fecha_radicado,
    r.actualizado_en::date AS fecha_ultima_actualizacion,
    CASE WHEN r.estado = 'RADICADO' THEN 1 ELSE 0 END AS es_radicado,
    CASE WHEN r.estado = 'EN_REVISION' THEN 1 ELSE 0 END AS es_en_revision,
    CASE WHEN r.estado = 'CONTESTADO' THEN 1 ELSE 0 END AS es_contestado,
    COALESCE(r.actualizado_en::date - r.creado_en::date, 0) AS dias_atencion
FROM reportes r
LEFT JOIN LATERAL (
    SELECT ce_sub.curso_id
    FROM curso_estudiante ce_sub
    INNER JOIN cursos c_sub ON ce_sub.curso_id = c_sub.id
    INNER JOIN anios_lectivos al_sub ON c_sub.anio_lectivo_id = al_sub.id
    WHERE ce_sub.estudiante_id = r.estudiante_id 
      AND al_sub.es_actual = true
    LIMIT 1
) ce ON true;
