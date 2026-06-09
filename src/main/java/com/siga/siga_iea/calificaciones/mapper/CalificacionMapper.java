package com.siga.siga_iea.calificaciones.mapper;

import com.siga.siga_iea.calificaciones.entity.Calificacion;
import com.siga.siga_iea.calificaciones.dto.CalificacionDTO;

public class CalificacionMapper {
    public static CalificacionDTO toDTO(Calificacion calificacion) {
        CalificacionDTO dto = new CalificacionDTO();
        if (calificacion != null) {
            dto.setId(calificacion.getId());
            dto.setDescripcion(calificacion.getDescripcion());
        }
        return dto;
    }
}

