package com.siga.siga_iea.clases.enums;

import java.util.Optional;

/**
 * Catálogo cerrado y canónico de grados académicos para SIGA-IEA.
 * Representa la oferta educativa institucional (Preescolar, Primaria, Secundaria y Media).
 * 
 * Regla de Integridad: No permite valores arbitrarios ni aplica defaults silenciosos.
 * Si un valor no pertenece a este catálogo, se rechaza inmediatamente con IllegalArgumentException.
 */
public enum GradoAcademico {
    TRANSICION("Transición", "TRANS", 0, "PREESCOLAR"),
    PRIMERO("1°", "1", 1, "PRIMARIA"),
    SEGUNDO("2°", "2", 2, "PRIMARIA"),
    TERCERO("3°", "3", 3, "PRIMARIA"),
    CUARTO("4°", "4", 4, "PRIMARIA"),
    QUINTO("5°", "5", 5, "PRIMARIA"),
    SEXTO("6°", "6", 6, "SECUNDARIA"),
    SEPTIMO("7°", "7", 7, "SECUNDARIA"),
    OCTAVO("8°", "8", 8, "SECUNDARIA"),
    NOVENO("9°", "9", 9, "SECUNDARIA"),
    DECIMO("10°", "10", 10, "MEDIA"),
    ONCE("11°", "11", 11, "MEDIA");

    private final String nombre;
    private final String codigoPrefijo;
    private final int orden;
    private final String nivel;

    GradoAcademico(String nombre, String codigoPrefijo, int orden, String nivel) {
        this.nombre = nombre;
        this.codigoPrefijo = codigoPrefijo;
        this.orden = orden;
        this.nivel = nivel;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoPrefijo() {
        return codigoPrefijo;
    }

    public int getOrden() {
        return orden;
    }

    public String getNivel() {
        return nivel;
    }

    /**
     * Determina si el grado corresponde al ciclo de Primaria o Preescolar (Transición a 5°).
     * En este ciclo, la asistencia se registra por JORNADA en bloque único.
     */
    public boolean esPrimariaOPreescolar() {
        return this.orden <= 5;
    }

    /**
     * Retorna el siguiente grado en la secuencia académica escolar.
     * Si es 11°, retorna null (los estudiantes avanzan al estado 'Graduado').
     */
    public GradoAcademico getSiguienteGrado() {
        if (this == ONCE) {
            return null;
        }
        for (GradoAcademico g : values()) {
            if (g.orden == this.orden + 1) {
                return g;
            }
        }
        return null;
    }

    /**
     * Resuelve un valor textual al valor canónico del enum.
     * Acepta entradas comunes como "Transición", "transicion", "0", "0°", "1", "1°", "11", "11°".
     *
     * @param valor Cadena con el grado a normalizar.
     * @return GradoAcademico canónico correspondiente.
     * @throws IllegalArgumentException si el valor es nulo, vacío o no reconocido.
     */
    public static GradoAcademico from(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El grado académico es obligatorio y no puede ser nulo o vacío.");
        }
        String clean = valor.trim();

        // 1. Coincidencia exacta con nombre canónico (ej: "Transición", "1°", "11°")
        for (GradoAcademico g : values()) {
            if (g.nombre.equalsIgnoreCase(clean)) {
                return g;
            }
        }

        // 2. Normalización de Transición / Preescolar / 0 / 0°
        String lower = clean.toLowerCase();
        if (lower.contains("trans") || lower.equals("0") || lower.equals("0°") || lower.contains("preescolar")) {
            return TRANSICION;
        }

        // 3. Normalización estricta de grados numéricos (ej: "1", "1°", "Grado 1", "11", "11°", "Grado 11°")
        if (clean.matches("(?i)^(grado\\s*)?\\d{1,2}°?$")) {
            String num = clean.replaceAll("[^0-9]", "");
            try {
                int n = Integer.parseInt(num);
                for (GradoAcademico g : values()) {
                    if (g.orden == n && g.orden > 0) {
                        return g;
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        throw new IllegalArgumentException(
                String.format("Grado académico '%s' no es válido. Valores permitidos: Transición, 1°, 2°, 3°, 4°, 5°, 6°, 7°, 8°, 9°, 10°, 11°", valor));
    }

    public static Optional<GradoAcademico> find(String valor) {
        try {
            return Optional.of(from(valor));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
