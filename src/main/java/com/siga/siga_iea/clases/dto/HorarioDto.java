package com.siga.siga_iea.clases.dto;

import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO para la creación, actualización y validación de horarios flexibles.
 */
public record HorarioDto(
        UUID cursoId,
        String diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        UUID materiaId,
        UUID docenteId,
        UUID salonId,
        String salon
) {
    public static HorarioDto of(UUID cursoId, String diaSemana, LocalTime horaInicio, LocalTime horaFin, UUID materiaId, UUID docenteId, String salon) {
        return new HorarioDto(cursoId, diaSemana, horaInicio, horaFin, materiaId, docenteId, null, salon);
    }
}
