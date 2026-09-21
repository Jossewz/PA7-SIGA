package com.siga.siga_iea.auth.enums;

/**
 * Enumeración fuertemente tipada de roles institucionales en SIGA-IEA.
 * Provee métodos de normalización y chequeo de roles para desacoplar el sistema
 * de strings mágicos.
 */
public enum RolEnum {
    ADMIN("ADMIN", "Administrador"),
    PERSONAL_ADMINISTRATIVO("PERSONAL_ADMINISTRATIVO", "Personal Administrativo"),
    DOCENTE("DOCENTE", "Docente"),
    ESTUDIANTE("ESTUDIANTE", "Estudiante");

    private final String codigo;
    private final String etiqueta;

    RolEnum(String codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    public boolean esAdmin() {
        return this == ADMIN;
    }

    public boolean esDocente() {
        return this == DOCENTE;
    }

    public boolean esPersonalAdministrativo() {
        return this == PERSONAL_ADMINISTRATIVO;
    }

    public boolean esEstudiante() {
        return this == ESTUDIANTE;
    }

    public boolean esAdminOAdministrativo() {
        return this == ADMIN || this == PERSONAL_ADMINISTRATIVO;
    }

    /**
     * Resuelve un RolEnum a partir de cualquier cadena cruda, normalizando espacios,
     * mayúsculas y alias institucionales (Rector, Coordinador, etc.).
     *
     * @param rawRole Cadena cruda con el rol
     * @return RolEnum correspondiente (por defecto ESTUDIANTE)
     */
    public static RolEnum from(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return ESTUDIANTE;
        }
        String clean = rawRole.trim().toUpperCase().replace(" ", "_");
        if (clean.contains("ADMIN") && !clean.contains("PERSONAL")) {
            return ADMIN;
        }
        if (clean.contains("PERSONAL") || clean.contains("RECTOR") || clean.contains("COORDINADOR") || clean.contains("SECRETARI")) {
            return PERSONAL_ADMINISTRATIVO;
        }
        if (clean.contains("DOCENTE")) {
            return DOCENTE;
        }
        if (clean.contains("ESTUDIANTE")) {
            return ESTUDIANTE;
        }
        for (RolEnum r : values()) {
            if (r.name().equalsIgnoreCase(clean) || r.codigo.equalsIgnoreCase(clean)) {
                return r;
            }
        }
        return ESTUDIANTE;
    }
}
