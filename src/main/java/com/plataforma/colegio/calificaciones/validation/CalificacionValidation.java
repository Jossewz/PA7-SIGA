package com.plataforma.colegio.calificaciones.validation;

public class CalificacionValidation {
    public boolean isValida(String descripcion) {
        return descripcion != null && !descripcion.isBlank();
    }
}
