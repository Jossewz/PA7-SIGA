-- =======================================================
-- SEED SCRIPT: 1.200 Estudiantes, 30 Cursos, Docentes y Horarios
-- Diseñado para pruebas de carga k6 y validación empírica
-- =======================================================

DO $$
DECLARE
    v_anio_lectivo_id UUID;
    v_admin_id UUID := 'a0000000-0000-0000-0000-000000000001';
    v_docente1_id UUID := 'd0000000-0000-0000-0000-000000000001';
    v_docente2_id UUID := 'd0000000-0000-0000-0000-000000000002';
    v_docente3_id UUID := 'd0000000-0000-0000-0000-000000000003';
    v_mat_id UUID := 'ba000000-0000-0000-0000-000000000001';
    v_esp_id UUID := 'ba000000-0000-0000-0000-000000000002';
    v_cie_id UUID := 'ba000000-0000-0000-0000-000000000003';
    v_curso_id UUID;
    v_estudiante_id UUID;
    v_curso_idx INT := 0;
    v_grado TEXT;
    v_grupo TEXT;
    v_cursos_array UUID[];
    v_docentes_array UUID[] := ARRAY[v_docente1_id, v_docente2_id, v_docente3_id];
    v_nombres_m TEXT[] := ARRAY['Santiago', 'Mateo', 'Sebastián', 'Matías', 'Samuel', 'Nicolás', 'Alejandro', 'Jerónimo', 'Emiliano', 'Diego', 'Daniel', 'Andrés', 'Felipe', 'Lucas', 'David', 'Joaquín', 'Tomás', 'Gabriel', 'Juan Pablo', 'Juan David'];
    v_nombres_f TEXT[] := ARRAY['Valentina', 'Sofía', 'Isabella', 'Camila', 'Mariana', 'Luciana', 'Gabriela', 'Daniela', 'Salomé', 'Valeria', 'Sara', 'María José', 'Antonella', 'Samantha', 'Paula', 'Martina', 'Elena', 'Victoria', 'Catalina', 'Ana Sofía'];
    v_apellidos TEXT[] := ARRAY['Gómez', 'Rodríguez', 'González', 'Martínez', 'García', 'López', 'Hernández', 'Sánchez', 'Ramírez', 'Pérez', 'Díaz', 'Muñoz', 'Rojas', 'Moreno', 'Jiménez', 'Ortiz', 'Castro', 'Vargas', 'Torres', 'Gutiérrez', 'Navarro', 'Morales', 'Ramos', 'Castillo', 'Guerrero', 'Mendoza', 'Suárez', 'Medina', 'Herrera', 'Valencia', 'Salazar', 'Quintero', 'Rincón', 'Cardona', 'Restrepo', 'Zapata', 'Ospina', 'Montoya', 'Bedoya', 'Cano'];
    v_est_nom TEXT;
    v_est_ape TEXT;
    v_est_gen VARCHAR(10);
BEGIN
    -- 0. Asegurar año lectivo 2026
    SELECT id INTO v_anio_lectivo_id FROM anios_lectivos WHERE anio = 2026;
    IF v_anio_lectivo_id IS NULL THEN
        v_anio_lectivo_id := 'e0000000-0000-0000-0000-000000002026';
        INSERT INTO anios_lectivos (id, anio, estado, es_actual)
        VALUES (v_anio_lectivo_id, 2026, 'ACTIVO', true)
        ON CONFLICT (anio) DO NOTHING;
    END IF;

    -- 1. Super Admin (admin@ieaci.edu.co / admin)
    INSERT INTO usuarios (id, email, password, rol, numero_documento, estado, created_at)
    VALUES (
        v_admin_id,
        'admin@ieaci.edu.co',
        '$2a$10$w4rU2Yy3gWkJXmQfI/6Pj.kH9cE9VqXFjC9I7v9E9J8Ym8F2Z1gS2', -- 'admin'
        'ADMIN',
        '0000000000',
        'Activo',
        NOW()
    ) ON CONFLICT (email) DO NOTHING;

    -- 2. Docentes de prueba (con usuarios para login)
    INSERT INTO docentes (id, nombres, apellidos, tipo_documento, numero_documento, estado, created_at)
    VALUES
        (v_docente1_id, 'Carlos', 'Mendoza', 'CC', '1010000001', 'Activo', NOW()),
        (v_docente2_id, 'Ana', 'García', 'CC', '1010000002', 'Activo', NOW()),
        (v_docente3_id, 'Jorge', 'Herrera', 'CC', '1010000003', 'Activo', NOW())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO usuarios (id, email, password, rol, numero_documento, estado, created_at)
    VALUES
        (gen_random_uuid(), 'carlos.mendoza@ieaci.edu.co', '$2a$10$Rd8jgEKgRMZG/fgS5zwF/eofwHyxjp6nLBvwBeAbHch7/i0TwEpBW', 'DOCENTE', '1010000001', 'Activo', NOW()),
        (gen_random_uuid(), 'ana.garcia@ieaci.edu.co', '$2a$10$Rd8jgEKgRMZG/fgS5zwF/eofwHyxjp6nLBvwBeAbHch7/i0TwEpBW', 'DOCENTE', '1010000002', 'Activo', NOW()),
        (gen_random_uuid(), 'jorge.herrera@ieaci.edu.co', '$2a$10$Rd8jgEKgRMZG/fgS5zwF/eofwHyxjp6nLBvwBeAbHch7/i0TwEpBW', 'DOCENTE', '1010000003', 'Activo', NOW())
    ON CONFLICT (email) DO NOTHING;

    -- 3. Materias
    INSERT INTO materias (id, nombre, area, intensidad_horaria, estado)
    VALUES
        (v_mat_id, 'Matemáticas', 'Ciencias Exactas', 4, 'Activo'),
        (v_esp_id, 'Lengua Castellana', 'Humanidades', 4, 'Activo'),
        (v_cie_id, 'Ciencias Naturales', 'Ciencias', 4, 'Activo')
    ON CONFLICT (id) DO NOTHING;

    -- 4. Crear 30 Cursos en rango válido 1° a 11° (sin grado 0°)
    -- Grados 1° a 4° (2 grupos cada uno = 8 cursos)
    -- Grados 5° a 10° (3 grupos cada uno = 18 cursos)
    -- Grado 11° (4 grupos = 4 cursos)
    -- Total: 30 cursos = 1.200 estudiantes
    FOR g IN 1..11 LOOP
        v_grado := g || '°';
        FOR grp IN 1..CASE WHEN g <= 4 THEN 2 WHEN g <= 10 THEN 3 ELSE 4 END LOOP
            v_grupo := LPAD(grp::text, 2, '0');
            v_curso_id := gen_random_uuid();
            
            INSERT INTO cursos (id, grado, grupo, jornada, cupos_maximos, ano_lectivo, anio_lectivo_id, estado, director_id, created_at)
            VALUES (v_curso_id, v_grado, v_grupo, 'Mañana', 40, '2026', v_anio_lectivo_id, 'Activo', v_docentes_array[(g % 3) + 1], NOW());

            v_cursos_array := array_append(v_cursos_array, v_curso_id);

            -- Si es secundaria (6° en adelante), vincular CursoMateria y Horarios
            IF g >= 6 THEN
                INSERT INTO curso_materia (id, curso_id, materia_id, docente_id, ano_lectivo, anio_lectivo_id)
                VALUES
                    (gen_random_uuid(), v_curso_id, v_mat_id, v_docente1_id, '2026', v_anio_lectivo_id),
                    (gen_random_uuid(), v_curso_id, v_esp_id, v_docente2_id, '2026', v_anio_lectivo_id),
                    (gen_random_uuid(), v_curso_id, v_cie_id, v_docente3_id, '2026', v_anio_lectivo_id)
                ON CONFLICT (curso_id, materia_id, anio_lectivo_id) DO NOTHING;

                -- Horarios base (Lunes a Viernes)
                FOR d IN 1..5 LOOP
                    INSERT INTO horarios (id, curso_id, materia_id, docente_id, dia_semana, hora_inicio, hora_fin, salon, salon_id)
                    VALUES
                        (gen_random_uuid(), v_curso_id, v_mat_id, v_docente1_id, (ARRAY['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'])[d], '07:00:00', '08:30:00', 'Aula 101', 'b0000000-0000-0000-0000-000000000101'),
                        (gen_random_uuid(), v_curso_id, v_esp_id, v_docente2_id, (ARRAY['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'])[d], '08:30:00', '10:00:00', 'Aula 102', 'b0000000-0000-0000-0000-000000000102'),
                        (gen_random_uuid(), v_curso_id, v_cie_id, v_docente3_id, (ARRAY['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'])[d], '10:30:00', '12:00:00', 'Aula 201', 'b0000000-0000-0000-0000-000000000201');
                END LOOP;
            END IF;
        END LOOP;
    END LOOP;

    -- 5. Crear 1.200 Estudiantes e inscribirlos exactamente 40 por curso
    FOR i IN 1..1200 LOOP
        v_estudiante_id := gen_random_uuid();
        v_curso_idx := ((i - 1) / 40) + 1; -- Cursos 1 a 30

        IF i % 2 = 0 THEN
            v_est_gen := 'M';
            v_est_nom := v_nombres_m[((i - 1) % array_length(v_nombres_m, 1)) + 1];
        ELSE
            v_est_gen := 'F';
            v_est_nom := v_nombres_f[((i - 1) % array_length(v_nombres_f, 1)) + 1];
        END IF;

        v_est_ape := v_apellidos[(((i - 1) * 3) % array_length(v_apellidos, 1)) + 1] || ' ' ||
                     v_apellidos[(((i - 1) * 7 + 13) % array_length(v_apellidos, 1)) + 1];

        INSERT INTO estudiantes (
            id, codigo, nombres, apellidos, tipo_documento, numero_documento,
            genero, telefono, estado, created_at
        ) VALUES (
            v_estudiante_id,
            'EST-2026-' || LPAD(i::text, 4, '0'),
            v_est_nom,
            v_est_ape,
            'TI',
            '1090' || LPAD(i::text, 6, '0'),
            v_est_gen,
            '300' || LPAD(i::text, 7, '0'),
            'Activo',
            NOW()
        );

        INSERT INTO curso_estudiante (id, curso_id, estudiante_id, ano_lectivo, anio_lectivo_id)
        VALUES (
            gen_random_uuid(),
            v_cursos_array[v_curso_idx],
            v_estudiante_id,
            '2026',
            v_anio_lectivo_id
        );
    END LOOP;

    RAISE NOTICE 'Seed completado exitosamente: 30 cursos creados con 40 estudiantes cada uno (Total: 1.200 estudiantes).';
END $$;
