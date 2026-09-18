package com.siga.siga_iea.asistencias.service;

import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.repository.AsistenciaRepository;
import com.siga.siga_iea.clases.entity.Clase;
import com.siga.siga_iea.clases.entity.Materia;
import com.siga.siga_iea.clases.repository.ClaseRepository;
import com.siga.siga_iea.clases.repository.MateriaRepository;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final ClaseRepository claseRepository;
    private final MateriaRepository materiaRepository;
    private final EstudianteRepository estudianteRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                             ClaseRepository claseRepository,
                             MateriaRepository materiaRepository,
                             EstudianteRepository estudianteRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.claseRepository = claseRepository;
        this.materiaRepository = materiaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public List<Asistencia> obtenerAsistencias(UUID cursoId, LocalDate fecha, UUID materiaId) {
        if (materiaId != null) {
            return asistenciaRepository.findByCursoIdAndMateriaIdAndFecha(cursoId, materiaId, fecha);
        }
        return asistenciaRepository.findByCursoIdAndFecha(cursoId, fecha);
    }

    public Map<UUID, String> obtenerMapaEstadosAsistencia(UUID cursoId, LocalDate fecha, UUID materiaId) {
        List<Asistencia> lista = obtenerAsistencias(cursoId, fecha, materiaId);
        Map<UUID, String> mapa = new HashMap<>();
        for (Asistencia a : lista) {
            if (a.getEstudiante() != null) {
                mapa.put(a.getEstudiante().getId(), a.getEstado());
            }
        }
        return mapa;
    }

    @Transactional
    public Asistencia toggleAsistencia(UUID cursoId, UUID estudianteId, LocalDate fecha, UUID materiaId) {
        Optional<Asistencia> opt = (materiaId != null)
                ? asistenciaRepository.findByCursoIdAndEstudianteIdAndFechaAndMateriaId(cursoId, estudianteId, fecha, materiaId)
                : asistenciaRepository.findByCursoIdAndEstudianteIdAndFecha(cursoId, estudianteId, fecha);

        Asistencia asistencia;
        if (opt.isPresent()) {
            asistencia = opt.get();
            String actual = asistencia.getEstado();
            String siguiente;
            if ("Presente".equalsIgnoreCase(actual)) {
                siguiente = "No presente";
            } else if ("No presente".equalsIgnoreCase(actual)) {
                siguiente = "Excusado";
            } else {
                siguiente = "Presente";
            }
            asistencia.setEstado(siguiente);
        } else {
            Clase curso = claseRepository.findById(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
            Estudiante estudiante = estudianteRepository.findById(estudianteId)
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
            Materia materia = (materiaId != null) ? materiaRepository.findById(materiaId).orElse(null) : null;

            asistencia = new Asistencia(curso, materia, estudiante, fecha, "No presente");
        }

        return asistenciaRepository.save(asistencia);
    }

    @Transactional
    public Asistencia registrarOActualizar(UUID cursoId, UUID estudianteId, LocalDate fecha, UUID materiaId, String estado, String observaciones) {
        Optional<Asistencia> opt = (materiaId != null)
                ? asistenciaRepository.findByCursoIdAndEstudianteIdAndFechaAndMateriaId(cursoId, estudianteId, fecha, materiaId)
                : asistenciaRepository.findByCursoIdAndEstudianteIdAndFecha(cursoId, estudianteId, fecha);

        Asistencia asistencia;
        if (opt.isPresent()) {
            asistencia = opt.get();
            asistencia.setEstado(estado);
            if (observaciones != null) asistencia.setObservaciones(observaciones);
        } else {
            Clase curso = claseRepository.findById(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
            Estudiante estudiante = estudianteRepository.findById(estudianteId)
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
            Materia materia = (materiaId != null) ? materiaRepository.findById(materiaId).orElse(null) : null;

            asistencia = new Asistencia(curso, materia, estudiante, fecha, estado);
            if (observaciones != null) asistencia.setObservaciones(observaciones);
        }

        return asistenciaRepository.save(asistencia);
    }
}
