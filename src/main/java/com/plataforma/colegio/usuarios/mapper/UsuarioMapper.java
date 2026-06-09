package com.plataforma.colegio.usuarios.mapper;

import com.plataforma.colegio.usuarios.entity.Usuario;
import com.plataforma.colegio.usuarios.dto.UsuarioDTO;

public class UsuarioMapper {
    public static UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        return dto;
    }
}
