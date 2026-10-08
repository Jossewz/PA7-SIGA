package com.siga.siga_iea.asistencias.dto;

import java.util.UUID;

/**
 * DTO que representa una fila de asistencia para la interfaz de toma de asistencia.
 *
 * @param id Identificador único del estudiante
 * @param nombre Nombre completo del estudiante (Apellidos, Nombres)
 * @param documento Número de identificación / documento del estudiante
 * @param estado Estado de asistencia registrado ("PRESENTE", "TARDE", "EXCUSADO", "AUSENTE") o null si está pendiente
 * @param observaciones Observaciones o anotaciones de la asistencia
 */
public record FilaAsistenciaDto(
        UUID id,
        String nombre,
        String documento,
        String estado,
        String observaciones
) {}
