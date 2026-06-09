package com.siga.siga_iea.clases.mapper;

import com.siga.siga_iea.clases.entity.Clase;
import com.siga.siga_iea.clases.dto.ClaseDTO;

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

