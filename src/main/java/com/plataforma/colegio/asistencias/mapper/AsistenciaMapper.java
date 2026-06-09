package com.plataforma.colegio.asistencias.mapper;

import com.plataforma.colegio.asistencias.dto.AsistenciaDTO;
import com.plataforma.colegio.asistencias.entity.Asistencia;

public class AsistenciaMapper {
    public static AsistenciaDTO toDTO(Asistencia asistencia) {
        AsistenciaDTO dto = new AsistenciaDTO();
        if (asistencia != null) {
            dto.setId(asistencia.getId());
            dto.setEstado(asistencia.getEstado());
        }
        return dto;
    }
}
