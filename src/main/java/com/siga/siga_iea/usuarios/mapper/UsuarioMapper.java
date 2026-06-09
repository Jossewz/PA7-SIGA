package com.siga.siga_iea.usuarios.mapper;

import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.dto.UsuarioDTO;

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

