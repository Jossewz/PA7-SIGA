package com.plataforma.colegio.matricula.validation;

public class MatriculaValidation {
    public boolean isValidCurso(String curso) {
        return curso != null && !curso.isBlank();
    }
}
