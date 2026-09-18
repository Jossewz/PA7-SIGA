package com.siga.siga_iea.calificaciones.service;

import com.siga.siga_iea.calificaciones.dto.BoletinMateriaDTO;
import com.siga.siga_iea.calificaciones.dto.EvaluacionResumenDTO;
import com.siga.siga_iea.calificaciones.entity.Calificacion;
import com.siga.siga_iea.calificaciones.entity.Evaluacion;
import com.siga.siga_iea.calificaciones.repository.CalificacionesRepository;
import com.siga.siga_iea.calificaciones.repository.EvaluacionRepository;
import com.siga.siga_iea.clases.entity.Clase;
import com.siga.siga_iea.clases.entity.CursoEstudiante;
import com.siga.siga_iea.clases.entity.CursoMateria;
import com.siga.siga_iea.clases.entity.Materia;
import com.siga.siga_iea.clases.repository.ClaseRepository;
import com.siga.siga_iea.clases.repository.CursoEstudianteRepository;
import com.siga.siga_iea.clases.repository.CursoMateriaRepository;
import com.siga.siga_iea.clases.repository.MateriaRepository;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CalificacionesService {

    private final CalificacionesRepository calificacionesRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final CursoMateriaRepository cursoMateriaRepository;
    private final EstudianteRepository estudianteRepository;
    private final ClaseRepository claseRepository;
    private final MateriaRepository materiaRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;

    public CalificacionesService(CalificacionesRepository calificacionesRepository,
                                 EvaluacionRepository evaluacionRepository,
                                 CursoMateriaRepository cursoMateriaRepository,
                                 EstudianteRepository estudianteRepository,
                                 ClaseRepository claseRepository,
                                 MateriaRepository materiaRepository,
                                 CursoEstudianteRepository cursoEstudianteRepository) {
        this.calificacionesRepository = calificacionesRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.estudianteRepository = estudianteRepository;
        this.claseRepository = claseRepository;
        this.materiaRepository = materiaRepository;
        this.cursoEstudianteRepository = cursoEstudianteRepository;
    }

    public List<Calificacion> obtenerCalificacionesEstudiante(UUID estudianteId) {
        return calificacionesRepository.findByEstudianteId(estudianteId);
    }

    @Transactional
    public CursoMateria obtenerOCrearCursoMateria(UUID cursoId, UUID materiaId, String anoLectivo) {
        return cursoMateriaRepository.findByCursoIdAndMateriaIdAndAnoLectivo(cursoId, materiaId, anoLectivo)
                .orElseGet(() -> {
                    Clase curso = claseRepository.findById(cursoId)
                            .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
                    Materia materia = materiaRepository.findById(materiaId)
                            .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada"));
                    CursoMateria cm = new CursoMateria(curso, materia, null, anoLectivo);
                    return cursoMateriaRepository.save(cm);
                });
    }

    @Transactional
    public CursoMateria obtenerOCrearCursoMateriaPorNombre(UUID cursoId, String materiaNombre, String anoLectivo) {
        Materia materia = materiaRepository.findByNombre(materiaNombre)
                .orElseGet(() -> materiaRepository.save(new Materia(materiaNombre, null)));
        return obtenerOCrearCursoMateria(cursoId, materia.getId(), anoLectivo);
    }

    @Transactional
    public Calificacion registrarONota(UUID evaluacionId, UUID estudianteId, BigDecimal nota, String observaciones) {
        Evaluacion ev = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluación no encontrada"));
        Estudiante est = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        Optional<Calificacion> opt = calificacionesRepository.findByEvaluacionIdAndEstudianteId(evaluacionId, estudianteId);
        Calificacion c = opt.orElseGet(() -> new Calificacion(ev, est, BigDecimal.ZERO));
        c.setNota(nota != null ? nota.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
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

    @Transactional
    public Evaluacion crearEvaluacionAutoEquitativa(UUID cursoMateriaId, Integer periodo, String fechaStr) {
        CursoMateria cm = cursoMateriaRepository.findById(cursoMateriaId)
                .orElseThrow(() -> new IllegalArgumentException("Asignatura de curso no encontrada"));

        List<Evaluacion> existentes = evaluacionRepository.findByCursoMateriaIdAndPeriodo(cursoMateriaId, periodo);
        int nuevaCantidad = existentes.size() + 1;
        BigDecimal pesoEquitativo = BigDecimal.valueOf(100.0 / nuevaCantidad).setScale(2, RoundingMode.HALF_UP);

        for (Evaluacion ev : existentes) {
            ev.setPeso(pesoEquitativo);
            evaluacionRepository.save(ev);
        }

        Evaluacion nueva = new Evaluacion(cm, "Nota " + nuevaCantidad, periodo, pesoEquitativo);
        if (fechaStr != null && !fechaStr.isBlank()) {
            try {
                nueva.setFecha(LocalDate.parse(fechaStr));
            } catch (Exception ignored) {}
        }
        return evaluacionRepository.save(nueva);
    }

    @Transactional
    public void eliminarEvaluacion(UUID evaluacionId) {
        Evaluacion ev = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluación no encontrada"));

        UUID cursoMateriaId = ev.getCursoMateria().getId();
        Integer periodo = ev.getPeriodo();

        calificacionesRepository.deleteByEvaluacionId(evaluacionId);
        evaluacionRepository.delete(ev);

        // Rebalancear pesos
        List<Evaluacion> restantes = evaluacionRepository.findByCursoMateriaIdAndPeriodo(cursoMateriaId, periodo);
        if (!restantes.isEmpty()) {
            BigDecimal nuevoPeso = BigDecimal.valueOf(100.0 / restantes.size()).setScale(2, RoundingMode.HALF_UP);
            for (Evaluacion r : restantes) {
                r.setPeso(nuevoPeso);
                evaluacionRepository.save(r);
            }
        }
    }

    @Transactional
    public void actualizarPesoEvaluacion(UUID evaluacionId, BigDecimal nuevoPeso) {
        Evaluacion ev = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluación no encontrada"));
        ev.setPeso(nuevoPeso != null ? nuevoPeso.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        evaluacionRepository.save(ev);
    }

    public List<Evaluacion> obtenerEvaluacionesPorCursoYMateria(UUID cursoMateriaId, Integer periodo) {
        if (periodo != null) {
            return evaluacionRepository.findByCursoMateriaIdAndPeriodo(cursoMateriaId, periodo);
        }
        return evaluacionRepository.findByCursoMateriaId(cursoMateriaId);
    }

    public Map<UUID, Map<UUID, BigDecimal>> obtenerCalificacionesMapa(UUID cursoMateriaId, Integer periodo) {
        List<Evaluacion> evaluaciones = obtenerEvaluacionesPorCursoYMateria(cursoMateriaId, periodo);
        Map<UUID, Map<UUID, BigDecimal>> mapaEstudiantes = new HashMap<>();

        for (Evaluacion ev : evaluaciones) {
            List<Calificacion> califs = calificacionesRepository.findByEvaluacionId(ev.getId());
            for (Calificacion c : califs) {
                if (c.getEstudiante() != null) {
                    mapaEstudiantes
                            .computeIfAbsent(c.getEstudiante().getId(), k -> new HashMap<>())
                            .put(ev.getId(), c.getNota());
                }
            }
        }
        return mapaEstudiantes;
    }

    public BigDecimal calcularNotaFinalPeriodo(UUID estudianteId, UUID cursoMateriaId, Integer periodo) {
        List<Evaluacion> evs = evaluacionRepository.findByCursoMateriaIdAndPeriodo(cursoMateriaId, periodo);
        if (evs.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sumaPonderada = BigDecimal.ZERO;
        BigDecimal sumaPesos = BigDecimal.ZERO;

        for (Evaluacion ev : evs) {
            Optional<Calificacion> opt = calificacionesRepository.findByEvaluacionIdAndEstudianteId(ev.getId(), estudianteId);
            BigDecimal nota = opt.map(Calificacion::getNota).orElse(BigDecimal.ZERO);
            BigDecimal peso = ev.getPeso() != null ? ev.getPeso() : BigDecimal.ZERO;

            sumaPonderada = sumaPonderada.add(nota.multiply(peso));
            sumaPesos = sumaPesos.add(peso);
        }

        if (sumaPesos.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return sumaPonderada.divide(sumaPesos, 2, RoundingMode.HALF_UP);
    }

    public List<BoletinMateriaDTO> obtenerBoletinEstudiante(UUID estudianteId, String anoLectivo, Map<Integer, BigDecimal> ponderacionesPeriodos) {
        List<CursoEstudiante> cursosEst = cursoEstudianteRepository.findByEstudianteId(estudianteId);
        List<BoletinMateriaDTO> listaBoletin = new ArrayList<>();

        if (ponderacionesPeriodos == null || ponderacionesPeriodos.isEmpty()) {
            ponderacionesPeriodos = new HashMap<>();
            ponderacionesPeriodos.put(1, new BigDecimal("30.00"));
            ponderacionesPeriodos.put(2, new BigDecimal("35.00"));
            ponderacionesPeriodos.put(3, new BigDecimal("35.00"));
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

        for (CursoEstudiante ce : cursosEst) {
            UUID cursoId = ce.getCurso().getId();
            List<CursoMateria> materiasDelCurso = cursoMateriaRepository.findByCursoId(cursoId);

            for (CursoMateria cm : materiasDelCurso) {
                BoletinMateriaDTO dto = new BoletinMateriaDTO(
                        cm.getId().toString(),
                        cm.getMateria().getId().toString(),
                        cm.getMateria().getNombre()
                );

                BigDecimal sumaPonderadaDefinitiva = BigDecimal.ZERO;

                for (int p = 1; p <= 3; p++) {
                    List<Evaluacion> evs = evaluacionRepository.findByCursoMateriaIdAndPeriodo(cm.getId(), p);
                    List<EvaluacionResumenDTO> evResList = new ArrayList<>();

                    BigDecimal sumaNotaXpeso = BigDecimal.ZERO;
                    BigDecimal sumaPesos = BigDecimal.ZERO;

                    for (Evaluacion ev : evs) {
                        Optional<Calificacion> cOpt = calificacionesRepository.findByEvaluacionIdAndEstudianteId(ev.getId(), estudianteId);
                        BigDecimal nota = cOpt.map(Calificacion::getNota).orElse(BigDecimal.ZERO);
                        String fechaDisp = ev.getFecha() != null ? ev.getFecha().format(dtf) : "";

                        BigDecimal peso = ev.getPeso() != null ? ev.getPeso() : BigDecimal.ZERO;

                        evResList.add(new EvaluacionResumenDTO(
                                ev.getId().toString(),
                                ev.getNombre(),
                                fechaDisp,
                                peso,
                                nota
                        ));

                        sumaNotaXpeso = sumaNotaXpeso.add(nota.multiply(peso));
                        sumaPesos = sumaPesos.add(peso);
                    }

                    BigDecimal promPeriodo = null;
                    if (!evs.isEmpty() && sumaPesos.compareTo(BigDecimal.ZERO) > 0) {
                        promPeriodo = sumaNotaXpeso.divide(sumaPesos, 2, RoundingMode.HALF_UP);
                    }

                    if (p == 1) {
                        dto.setPeriodo1Nota(promPeriodo);
                        dto.setEvaluacionesP1(evResList);
                    } else if (p == 2) {
                        dto.setPeriodo2Nota(promPeriodo);
                        dto.setEvaluacionesP2(evResList);
                    } else {
                        dto.setPeriodo3Nota(promPeriodo);
                        dto.setEvaluacionesP3(evResList);
                    }

                    if (promPeriodo != null) {
                        BigDecimal pesoPeriodo = ponderacionesPeriodos.getOrDefault(p, new BigDecimal("33.33"));
                        BigDecimal contribucion = promPeriodo.multiply(pesoPeriodo).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                        sumaPonderadaDefinitiva = sumaPonderadaDefinitiva.add(contribucion);
                    }
                }

                dto.setNotaDefinitiva(sumaPonderadaDefinitiva.setScale(2, RoundingMode.HALF_UP));
                listaBoletin.add(dto);
            }
        }

        return listaBoletin;
    }
}
