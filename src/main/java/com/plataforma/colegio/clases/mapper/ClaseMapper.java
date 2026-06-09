package com.plataforma.colegio.clases.mapper;

import com.plataforma.colegio.clases.entity.Clase;
import com.plataforma.colegio.clases.dto.ClaseDTO;

public class ClaseMapper {
    public static ClaseDTO toDTO(Clase clase) {
        ClaseDTO dto = new ClaseDTO();
        if (clase != null) {
            dto.setId(clase.getId());
            dto.setNombre(clase.getNombre());
        }
        return dto;
    }
}
