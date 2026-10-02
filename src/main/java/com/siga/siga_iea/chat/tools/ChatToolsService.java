package com.siga.siga_iea.chat.tools;

import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.repository.AsistenciaRepository;
import com.siga.siga_iea.auth.service.CurrentUserContextService;
import com.siga.siga_iea.calificaciones.entity.Calificacion;
import com.siga.siga_iea.calificaciones.repository.CalificacionesRepository;
import com.siga.siga_iea.chat.dto.ToolExecutionResult;
import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.CursoEstudiante;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.clases.repository.CursoEstudianteRepository;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.clases.repository.HorarioRepository;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Catálogo de herramientas seguras de SOLO LECTURA para el Chatbot Escolar.
 * Aplica el principio de mínimo privilegio y minimización bajo la Ley 1581 de 2012:
 * - El LLM jamás recibe permisos SQL ni de escritura.
 * - Las consultas de estudiantes resuelven la identidad desde la sesión (sin parámetros de ID libres).
 * - Los docentes solo pueden consultar cursos y estudiantes bajo su asignación académica.
 */
@Service
@Transactional(readOnly = true)
public class ChatToolsService {

    private final CurrentUserContextService userContextService;
    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;
    private final CursoRepository cursoRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;
    private final CalificacionesRepository calificacionesRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final HorarioRepository horarioRepository;

    public ChatToolsService(CurrentUserContextService userContextService,
                            EstudianteRepository estudianteRepository,
                            DocenteRepository docenteRepository,
                            CursoRepository cursoRepository,
                            CursoEstudianteRepository cursoEstudianteRepository,
                            CalificacionesRepository calificacionesRepository,
                            AsistenciaRepository asistenciaRepository,
                            HorarioRepository horarioRepository) {
        this.userContextService = userContextService;
        this.estudianteRepository = estudianteRepository;
        this.docenteRepository = docenteRepository;
        this.cursoRepository = cursoRepository;
        this.cursoEstudianteRepository = cursoEstudianteRepository;
        this.calificacionesRepository = calificacionesRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.horarioRepository = horarioRepository;
    }

    // =========================================================================
    // HERRAMIENTAS ROL ESTUDIANTE (Identidad atada a la sesión)
    // =========================================================================

    public ToolExecutionResult consultarMisNotas() {
        Estudiante estudiante = userContextService.getEstudianteAutenticado()
                .orElseThrow(() -> new AccessDeniedException("No se encontró estudiante vinculado a la sesión actual."));

        List<Calificacion> califs = calificacionesRepository.findByEstudianteId(estudiante.getId());

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("estudiante", estudiante.getNombres() + " " + estudiante.getApellidos().charAt(0) + ".");
        datos.put("codigo", estudiante.getCodigo());

        List<Map<String, Object>> listaNotas = califs.stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("materia", c.getEvaluacion().getCursoMateria().getMateria().getNombre());
            item.put("evaluacion", c.getEvaluacion().getNombre());
            item.put("periodo", c.getEvaluacion().getPeriodo());
            item.put("porcentaje", c.getEvaluacion().getPeso() != null ? c.getEvaluacion().getPeso() + "%" : "N/A");
            item.put("nota", c.getNota() != null ? c.getNota().doubleValue() : "Pendiente");
            return item;
        }).collect(Collectors.toList());

        datos.put("calificaciones", listaNotas);
        datos.put("totalCalificaciones", listaNotas.size());

        return new ToolExecutionResult("consultarMisNotas", true, "Notas del estudiante autenticado", datos);
    }

    public ToolExecutionResult consultarMiHorarioHoy() {
        Estudiante estudiante = userContextService.getEstudianteAutenticado()
                .orElseThrow(() -> new AccessDeniedException("No se encontró estudiante vinculado a la sesión actual."));

        List<CursoEstudiante> inscripciones = cursoEstudianteRepository.findByEstudianteId(estudiante.getId());
        if (inscripciones.isEmpty()) {
            return new ToolExecutionResult("consultarMiHorarioHoy", true, "Sin curso asignado", Map.of("horarios", List.of()));
        }

        Curso curso = inscripciones.get(0).getCurso();
        DayOfWeek dia = LocalDate.now().getDayOfWeek();
        String diaEspanol = traducirDiaSemana(dia);

        List<Horario> horarios = horarioRepository.findByCursoId(curso.getId());
        List<Map<String, Object>> bloquesHoy = horarios.stream()
                .filter(h -> h.getDiaSemana() != null && h.getDiaSemana().equalsIgnoreCase(diaEspanol))
                .sorted(Comparator.comparing(Horario::getHoraInicio))
                .map(h -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("horaInicio", h.getHoraInicio() != null ? h.getHoraInicio().toString() : "--:--");
                    map.put("horaFin", h.getHoraFin() != null ? h.getHoraFin().toString() : "--:--");
                    map.put("materia", h.getMateria() != null ? h.getMateria().getNombre() : "Jornada");
                    map.put("salon", h.getSalonNombre());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("curso", curso.getNombre());
        datos.put("dia", diaEspanol);
        datos.put("bloques", bloquesHoy);

        return new ToolExecutionResult("consultarMiHorarioHoy", true, "Horario de hoy para el curso del estudiante", datos);
    }

    public ToolExecutionResult consultarMiResumenAsistencia() {
        Estudiante estudiante = userContextService.getEstudianteAutenticado()
                .orElseThrow(() -> new AccessDeniedException("No se encontró estudiante vinculado a la sesión actual."));

        List<Asistencia> asistencias = asistenciaRepository.findByEstudianteId(estudiante.getId());

        long presentes = asistencias.stream().filter(a -> "P".equalsIgnoreCase(a.getEstado())).count();
        long justificadas = asistencias.stream().filter(a -> "FJ".equalsIgnoreCase(a.getEstado())).count();
        long injustificadas = asistencias.stream().filter(a -> "FI".equalsIgnoreCase(a.getEstado())).count();
        long retrasos = asistencias.stream().filter(a -> "R".equalsIgnoreCase(a.getEstado())).count();

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("totalRegistros", asistencias.size());
        datos.put("asistenciasPresente", presentes);
        datos.put("fallasJustificadas", justificadas);
        datos.put("fallasInjustificadas", injustificadas);
        datos.put("retrasos", retrasos);

        return new ToolExecutionResult("consultarMiResumenAsistencia", true, "Resumen de asistencias del estudiante", datos);
    }

    // =========================================================================
    // HERRAMIENTAS ROL DOCENTE (Validación de asignación académica)
    // =========================================================================

    public ToolExecutionResult consultarMisCursos() {
        Docente docente = userContextService.getDocenteAutenticado()
                .orElseThrow(() -> new AccessDeniedException("Se requiere rol docente con cuenta activa."));

        List<Curso> cursosDirector = cursoRepository.findByDirectorId(docente.getId());

        List<Map<String, Object>> lista = cursosDirector.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cursoId", c.getId().toString());
            m.put("nombre", c.getNombre());
            m.put("grado", c.getGrado());
            m.put("grupo", c.getGrupo());
            m.put("jornada", c.getJornada());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("docente", docente.getNombres() + " " + docente.getApellidos());
        datos.put("totalCursosDirector", lista.size());
        datos.put("cursos", lista);

        return new ToolExecutionResult("consultarMisCursos", true, "Cursos asignados al docente", datos);
    }

    public ToolExecutionResult consultarEstudiantesEnRiesgo(UUID cursoId) {
        Docente docente = userContextService.getDocenteAutenticado()
                .orElseThrow(() -> new AccessDeniedException("Se requiere rol docente para consultar estudiantes en riesgo."));

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado."));

        // Validar que el docente sea director o tenga asignación
        boolean esDirector = curso.getDirector() != null && curso.getDirector().getId().equals(docente.getId());
        if (!esDirector && !userContextService.esAdminOAdministrativo()) {
            throw new AccessDeniedException("Acceso denegado: No tiene permisos de supervisión sobre el curso " + curso.getNombre());
        }

        List<CursoEstudiante> matriculados = cursoEstudianteRepository.findByCursoId(cursoId);
        List<Map<String, Object>> estudiantesEnRiesgo = new ArrayList<>();

        for (CursoEstudiante ce : matriculados) {
            Estudiante est = ce.getEstudiante();
            List<Calificacion> califs = calificacionesRepository.findByEstudianteId(est.getId());
            long reprobadas = califs.stream()
                    .filter(c -> c.getNota() != null && c.getNota().doubleValue() < 3.0)
                    .count();

            if (reprobadas > 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("codigo", est.getCodigo());
                item.put("nombre", est.getNombres() + " " + est.getApellidos().charAt(0) + ".");
                item.put("evaluacionesReprobadas", reprobadas);
                estudiantesEnRiesgo.add(item);
            }
        }

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("curso", curso.getNombre());
        datos.put("totalMatriculados", matriculados.size());
        datos.put("estudiantesEnRiesgo", estudiantesEnRiesgo);

        return new ToolExecutionResult("consultarEstudiantesEnRiesgo", true, "Estudiantes con asignaturas reprobadas", datos);
    }

    // =========================================================================
    // HERRAMIENTAS ROL ADMINISTRATIVO / DIRECTIVO
    // =========================================================================

    public ToolExecutionResult consultarEstadisticasGenerales() {
        if (!userContextService.esAdminOAdministrativo()) {
            throw new AccessDeniedException("Acceso restringido a personal directivo y administrativo.");
        }

        long totalEstudiantes = estudianteRepository.count();
        long totalDocentes = docenteRepository.count();
        long totalCursos = cursoRepository.count();

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("institucion", "Institución Educativa Ambientalista de Cartagena de Indias (IEACI)");
        datos.put("totalEstudiantesMatriculados", totalEstudiantes);
        datos.put("totalDocentesActivos", totalDocentes);
        datos.put("totalCursosActivos", totalCursos);
        datos.put("fechaReporte", LocalDate.now().toString());

        return new ToolExecutionResult("consultarEstadisticasGenerales", true, "Estadísticas globales de la IEACI", datos);
    }

    // =========================================================================
    // UTILIDADES PRIVADAS
    // =========================================================================

    private String traducirDiaSemana(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
}
