package com.siga.siga_iea.calificaciones.service;

import com.siga.siga_iea.calificaciones.entity.Calificacion;
import com.siga.siga_iea.calificaciones.entity.Evaluacion;
import com.siga.siga_iea.calificaciones.repository.CalificacionesRepository;
import com.siga.siga_iea.calificaciones.repository.EvaluacionRepository;
import com.siga.siga_iea.clases.entity.CursoMateria;
import com.siga.siga_iea.clases.repository.CursoMateriaRepository;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CalificacionesService {

    private final CalificacionesRepository calificacionesRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final CursoMateriaRepository cursoMateriaRepository;
    private final EstudianteRepository estudianteRepository;

    public CalificacionesService(CalificacionesRepository calificacionesRepository,
                                 EvaluacionRepository evaluacionRepository,
                                 CursoMateriaRepository cursoMateriaRepository,
                                 EstudianteRepository estudianteRepository) {
        this.calificacionesRepository = calificacionesRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public List<Calificacion> obtenerCalificacionesEstudiante(UUID estudianteId) {
        return calificacionesRepository.findByEstudianteId(estudianteId);
    }

    @Transactional
    public Calificacion registrarONota(UUID evaluacionId, UUID estudianteId, BigDecimal nota, String observaciones) {
        Evaluacion ev = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluación no encontrada"));
        Estudiante est = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        Optional<Calificacion> opt = calificacionesRepository.findByEvaluacionIdAndEstudianteId(evaluacionId, estudianteId);
        Calificacion c = opt.orElseGet(() -> new Calificacion(ev, est, BigDecimal.ZERO));
        c.setNota(nota);
        if (observaciones != null) c.setObservaciones(observaciones);

        return calificacionesRepository.save(c);
    }

    @Transactional
    public Evaluacion crearEvaluacion(UUID cursoMateriaId, String nombre, Integer periodo, BigDecimal peso) {
        CursoMateria cm = cursoMateriaRepository.findById(cursoMateriaId)
                .orElseThrow(() -> new IllegalArgumentException("Asignatura de curso no encontrada"));

        Evaluacion ev = new Evaluacion(cm, nombre, periodo, peso);
        return evaluacionRepository.save(ev);
    }

    public List<Evaluacion> obtenerEvaluacionesPorCursoYMateria(UUID cursoMateriaId, Integer periodo) {
        if (periodo != null) {
            return evaluacionRepository.findByCursoMateriaIdAndPeriodo(cursoMateriaId, periodo);
        }
        return evaluacionRepository.findByCursoMateriaId(cursoMateriaId);
    }

    public List<Calificacion> obtenerCalificacionesPorCursoYPeriodo(UUID cursoId, Integer periodo) {
        return calificacionesRepository.findByCursoAndPeriodo(cursoId, periodo);
    }
}
