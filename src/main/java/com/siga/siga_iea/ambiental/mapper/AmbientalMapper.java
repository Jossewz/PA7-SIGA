package com.siga.siga_iea.ambiental.mapper;

import com.siga.siga_iea.ambiental.entity.ActividadAmbiental;
import com.siga.siga_iea.ambiental.dto.AmbientalDTO;

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

