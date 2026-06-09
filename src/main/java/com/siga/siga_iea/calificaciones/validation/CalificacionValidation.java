package com.siga.siga_iea.calificaciones.validation;

public class CalificacionValidation {
    public boolean isValida(String descripcion) {
        return descripcion != null && !descripcion.isBlank();
    }
}

