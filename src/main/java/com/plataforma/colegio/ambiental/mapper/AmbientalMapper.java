package com.plataforma.colegio.ambiental.mapper;

import com.plataforma.colegio.ambiental.entity.ActividadAmbiental;
import com.plataforma.colegio.ambiental.dto.AmbientalDTO;

public class AmbientalMapper {
    public static AmbientalDTO toDTO(ActividadAmbiental actividad) {
        AmbientalDTO dto = new AmbientalDTO();
        if (actividad != null) {
            dto.setId(actividad.getId());
            dto.setNombre(actividad.getNombre());
        }
        return dto;
    }
}
