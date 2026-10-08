DO $$
DECLARE
    v_nombres_m TEXT[] := ARRAY['Santiago', 'Mateo', 'Sebastián', 'Matías', 'Samuel', 'Nicolás', 'Alejandro', 'Jerónimo', 'Emiliano', 'Diego', 'Daniel', 'Andrés', 'Felipe', 'Lucas', 'David', 'Joaquín', 'Tomás', 'Gabriel', 'Juan Pablo', 'Juan David'];
    v_nombres_f TEXT[] := ARRAY['Valentina', 'Sofía', 'Isabella', 'Camila', 'Mariana', 'Luciana', 'Gabriela', 'Daniela', 'Salomé', 'Valeria', 'Sara', 'María José', 'Antonella', 'Samantha', 'Paula', 'Martina', 'Elena', 'Victoria', 'Catalina', 'Ana Sofía'];
    v_apellidos TEXT[] := ARRAY['Gómez', 'Rodríguez', 'González', 'Martínez', 'García', 'López', 'Hernández', 'Sánchez', 'Ramírez', 'Pérez', 'Díaz', 'Muñoz', 'Rojas', 'Moreno', 'Jiménez', 'Ortiz', 'Castro', 'Vargas', 'Torres', 'Gutiérrez', 'Navarro', 'Morales', 'Ramos', 'Castillo', 'Guerrero', 'Mendoza', 'Suárez', 'Medina', 'Herrera', 'Valencia', 'Salazar', 'Quintero', 'Rincón', 'Cardona', 'Restrepo', 'Zapata', 'Ospina', 'Montoya', 'Bedoya', 'Cano'];
    r RECORD;
    i INT := 1;
    v_nom TEXT;
    v_ape TEXT;
BEGIN
    FOR r IN (SELECT id, genero FROM estudiantes ORDER BY codigo ASC) LOOP
        IF r.genero = 'F' THEN
            v_nom := v_nombres_f[((i - 1) % array_length(v_nombres_f, 1)) + 1];
        ELSE
            v_nom := v_nombres_m[((i - 1) % array_length(v_nombres_m, 1)) + 1];
        END IF;
        v_ape := v_apellidos[(((i - 1) * 3) % array_length(v_apellidos, 1)) + 1] || ' ' ||
                 v_apellidos[(((i - 1) * 7 + 13) % array_length(v_apellidos, 1)) + 1];
        
        UPDATE estudiantes 
        SET nombres = v_nom, apellidos = v_ape 
        WHERE id = r.id;
        
        i := i + 1;
    END LOOP;
END $$;
