package com.siga.siga_iea.asistencias.dto;

import java.util.UUID;

public record AsistenciaItemDto(
        UUID estudianteId,
        String estado,
        String observaciones
) {
    public AsistenciaItemDto(UUID estudianteId, String estado) {
        this(estudianteId, estado, null);
    }
}
