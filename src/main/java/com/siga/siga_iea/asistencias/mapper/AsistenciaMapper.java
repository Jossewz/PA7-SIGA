package com.siga.siga_iea.asistencias.mapper;

import com.siga.siga_iea.asistencias.dto.AsistenciaDTO;
import com.siga.siga_iea.asistencias.entity.Asistencia;

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

