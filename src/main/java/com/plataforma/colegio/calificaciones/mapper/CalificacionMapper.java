package com.plataforma.colegio.calificaciones.mapper;

import com.plataforma.colegio.calificaciones.entity.Calificacion;
import com.plataforma.colegio.calificaciones.dto.CalificacionDTO;

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
