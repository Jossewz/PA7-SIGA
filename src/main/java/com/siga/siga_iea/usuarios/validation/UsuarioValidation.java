package com.siga.siga_iea.usuarios.validation;

public class UsuarioValidation {
    public boolean validateNombre(String nombre) {
        return nombre != null && !nombre.trim().isEmpty();
    }
}

