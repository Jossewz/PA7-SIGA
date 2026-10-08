package com.siga.siga_iea.asistencias.service;

import com.siga.siga_iea.asistencias.dto.FilaAsistenciaDto;
import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.entity.SesionClase;
import com.siga.siga_iea.asistencias.repository.AsistenciaRepository;
import com.siga.siga_iea.clases.entity.CursoEstudiante;
import com.siga.siga_iea.clases.repository.CursoEstudianteRepository;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio optimizado para la construcción de la tabla de asistencia de una sesión.
 * Cumple estrictamente con la regla de 2 consultas únicas (sin N+1):
 * 1. Estudiantes matriculados en el curso (CursoEstudiante)
 * 2. Registros de asistencia existentes para la sesión (Asistencia)
 * Las filas se consolidan en memoria asociando estudiante y estado (null si está pendiente).
 */
@Service
public class AsistenciaTablaService {

    private final CursoEstudianteRepository cursoEstudianteRepository;
    private final AsistenciaRepository asistenciaRepository;

    public AsistenciaTablaService(CursoEstudianteRepository cursoEstudianteRepository,
                                 AsistenciaRepository asistenciaRepository) {
        this.cursoEstudianteRepository = cursoEstudianteRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    @Transactional(readOnly = true)
    public List<FilaAsistenciaDto> filas(SesionClase sesion) {
        if (sesion == null || sesion.getCurso() == null) {
            return Collections.emptyList();
        }

        // Consulta 1: Estudiantes matriculados en el curso
        List<CursoEstudiante> inscripciones = cursoEstudianteRepository.findByCursoId(sesion.getCurso().getId());

        // Consulta 2: Asistencias registradas para esta sesión
        List<Asistencia> asistencias = asistenciaRepository.findBySesionId(sesion.getId());

        // Mapeo en memoria por estudianteId (O(N))
        Map<UUID, Asistencia> mapaAsistencias = asistencias.stream()
                .filter(a -> a.getEstudiante() != null)
                .collect(Collectors.toMap(
                        a -> a.getEstudiante().getId(),
                        a -> a,
                        (a1, a2) -> a1
                ));

        List<FilaAsistenciaDto> filas = new ArrayList<>();
        for (CursoEstudiante ce : inscripciones) {
            Estudiante e = ce.getEstudiante();
            if (e == null) continue;

            Asistencia reg = mapaAsistencias.get(e.getId());
            String estado = reg != null ? reg.getEstado() : null;
            String observaciones = reg != null ? reg.getObservaciones() : null;

            String nombreCompleto = formatearNombre(e);

            filas.add(new FilaAsistenciaDto(
                    e.getId(),
                    nombreCompleto,
                    e.getNumeroDocumento(),
                    estado,
                    observaciones
            ));
        }

        // Ordenamiento alfabético por apellidos / nombres
        filas.sort(Comparator.comparing(FilaAsistenciaDto::nombre, String.CASE_INSENSITIVE_ORDER));
        return filas;
    }

    private String formatearNombre(Estudiante e) {
        String apellidos = e.getApellidos() != null ? e.getApellidos().trim() : "";
        String nombres = e.getNombres() != null ? e.getNombres().trim() : "";
        if (!apellidos.isEmpty() && !nombres.isEmpty()) {
            return apellidos + ", " + nombres;
        }
        return !apellidos.isEmpty() ? apellidos : nombres;
    }
}
