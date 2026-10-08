package com.siga.siga_iea.clases.controller;

import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.service.AsistenciaService;
import com.siga.siga_iea.auth.service.CurrentUserContextService;
import com.siga.siga_iea.calificaciones.service.CalificacionesService;
import com.siga.siga_iea.clases.application.CursoGestionAppService;
import com.siga.siga_iea.clases.dto.CursoGestionDetalleDTO;
import com.siga.siga_iea.clases.dto.CursoTablaNotasDTO;
import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.CursoEstudiante;
import com.siga.siga_iea.clases.entity.CursoMateria;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.clases.service.CursoService;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.service.PersonalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Controlador Web para Cursos y Clases.
 * Delegador liviano de peticiones HTTP hacia la capa de casos de uso y
 * servicios de aplicación.
 */
@Controller
public class CursoController {

    private final CursoService cursoService;
    private final CursoGestionAppService cursoGestionAppService;
    private final PersonalService personalService;
    private final CalificacionesService calificacionesService;
    private final AsistenciaService asistenciaService;
    private final CurrentUserContextService currentUserContextService;

    public CursoController(CursoService cursoService,
            CursoGestionAppService cursoGestionAppService,
            PersonalService personalService,
            CalificacionesService calificacionesService,
            AsistenciaService asistenciaService,
            CurrentUserContextService currentUserContextService) {
        this.cursoService = cursoService;
        this.cursoGestionAppService = cursoGestionAppService;
        this.personalService = personalService;
        this.calificacionesService = calificacionesService;
        this.asistenciaService = asistenciaService;
        this.currentUserContextService = currentUserContextService;
    }

    public static String obtenerNombreDiaEspanol(DayOfWeek dow) {
        return CursoGestionAppService.obtenerNombreDiaEspanol(dow);
    }

    private static final String[] PALETA_COLORES = {
            "#0d4117", // Verde Bosque SIGA
            "#059669", // Esmeralda
            "#0284c7", // Azul Cielo
            "#4f46e5", // Índigo
            "#7c3aed", // Púrpura
            "#d97706", // Ámbar
            "#dc2626", // Carmesí
            "#0d9488", // Verde Azulado
            "#c026d3", // Fucsia
            "#334155" // Pizarra
    };

    @GetMapping("/clases/horarios/datos")
    @ResponseBody
    public List<Map<String, Object>> obtenerHorariosDatosJson(@RequestParam("cursoId") UUID cursoId) {
        List<Horario> horarios = cursoService.listarHorariosDeCurso(cursoId);
        List<Map<String, Object>> res = new ArrayList<>();
        for (Horario h : horarios) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", h.getId().toString());
            map.put("dia", h.getDiaSemana());
            map.put("horaInicio", h.getHoraInicio() != null ? h.getHoraInicio().toString() : "07:00");
            map.put("horaFin", h.getHoraFin() != null ? h.getHoraFin().toString() : "08:30");
            map.put("materiaId", h.getMateria() != null ? h.getMateria().getId().toString() : "");
            map.put("materiaNombre", h.getMateria() != null ? h.getMateria().getNombre() : "Sin Asignatura");
            map.put("docenteId", h.getDocente() != null ? h.getDocente().getId().toString() : "");
            map.put("docenteNombre", h.getDocente() != null ? h.getDocente().getNombreCompleto() : "Sin Docente");
            map.put("salon", h.getSalonNombre());
            map.put("salonId", h.getSalonEntidad() != null ? h.getSalonEntidad().getId().toString() : "");

            // Color determinista según el nombre de la materia
            int hash = Math.abs(h.getMateria() != null ? h.getMateria().getNombre().hashCode() : 0);
            String color = PALETA_COLORES[hash % PALETA_COLORES.length];
            map.put("color", color);

            res.add(map);
        }
        return res;
    }

    @PostMapping("/clases/horarios/guardar-item")
    @ResponseBody
    public Map<String, Object> guardarHorarioItem(
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam("diaSemana") String diaSemana,
            @RequestParam(value = "horarioId", required = false) String horarioIdStr,
            @RequestParam("materiaId") UUID materiaId,
            @RequestParam(value = "docenteId", required = false) String docenteIdStr,
            @RequestParam("horaInicio") String horaInicio,
            @RequestParam("horaFin") String horaFin,
            @RequestParam(value = "salon", required = false) String salon) {

        Map<String, Object> res = new HashMap<>();
        try {
            UUID horarioId = (horarioIdStr != null && !horarioIdStr.isBlank() && !horarioIdStr.equals("null"))
                    ? UUID.fromString(horarioIdStr)
                    : null;
            UUID docenteId = (docenteIdStr != null && !docenteIdStr.isBlank() && !docenteIdStr.equals("null"))
                    ? UUID.fromString(docenteIdStr)
                    : null;

            Horario h;
            if (horarioId != null) {
                h = cursoService.actualizarHorario(horarioId, cursoId, diaSemana, materiaId, docenteId, horaInicio,
                        horaFin, salon);
            } else {
                h = cursoService.guardarHorarioBloque(cursoId, diaSemana, materiaId, docenteId, horaInicio, horaFin,
                        salon);
            }

            res.put("success", true);
            res.put("id", h.getId().toString());
            res.put("dia", h.getDiaSemana());
            res.put("horaInicio", h.getHoraInicio() != null ? h.getHoraInicio().toString() : horaInicio);
            res.put("horaFin", h.getHoraFin() != null ? h.getHoraFin().toString() : horaFin);
            res.put("materiaId", h.getMateria() != null ? h.getMateria().getId().toString() : "");
            res.put("materiaNombre", h.getMateria() != null ? h.getMateria().getNombre() : "");
            res.put("docenteId", h.getDocente() != null ? h.getDocente().getId().toString() : "");
            res.put("docenteNombre", h.getDocente() != null ? h.getDocente().getNombreCompleto() : "Sin Docente");
            res.put("salon", h.getSalonNombre());

            int hash = Math.abs(h.getMateria() != null ? h.getMateria().getNombre().hashCode() : 0);
            res.put("color", PALETA_COLORES[hash % PALETA_COLORES.length]);
        } catch (Exception ex) {
            res.put("success", false);
            res.put("error", ex.getMessage());
        }
        return res;
    }

    @PostMapping("/clases/horarios/eliminar-item")
    @ResponseBody
    public Map<String, Object> eliminarHorarioItem(@RequestParam("horarioId") UUID horarioId) {
        Map<String, Object> res = new HashMap<>();
        try {
            cursoService.eliminarHorario(horarioId);
            res.put("success", true);
        } catch (Exception ex) {
            res.put("success", false);
            res.put("error", ex.getMessage());
        }
        return res;
    }

    @PostMapping("/clases/horarios/limpiar")
    @ResponseBody
    public Map<String, Object> limpiarHorario(@RequestParam("cursoId") UUID cursoId) {
        Map<String, Object> res = new HashMap<>();
        try {
            cursoService.limpiarHorarioCurso(cursoId);
            res.put("success", true);
        } catch (Exception ex) {
            res.put("success", false);
            res.put("error", ex.getMessage());
        }
        return res;
    }

    @GetMapping("/clases")
    public String index(Model model) {
        model.addAttribute("title", "Gestión de Cursos – IEACI");
        model.addAttribute("activePage", "clases");

        List<Curso> cursosDB = cursoService.listarCursosPorAno("2026");
        List<Map<String, Object>> cursosList = new ArrayList<>();

        for (Curso c : cursosDB) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId().toString());
            map.put("grado", c.getGrado());
            map.put("grupo", c.getGrupo());
            map.put("codigoCurso", c.getCodigoCurso());
            map.put("director", c.getDirector() != null ? c.getDirector().getNombreCompleto() : "Sin asignar");
            map.put("directorId", c.getDirector() != null ? c.getDirector().getId().toString() : "");
            map.put("cuposMaximos", c.getCuposMaximos());

            List<CursoEstudiante> estudiantes = cursoService.listarEstudiantesDeCurso(c.getId());
            map.put("estudiantes", estudiantes.size());

            List<Horario> horarios = cursoService.listarHorariosDeCurso(c.getId());
            boolean tieneHorario = !horarios.isEmpty();
            map.put("tieneHorario", tieneHorario);
            map.put("horarioResumen", tieneHorario ? "Lun-Vie: 07:00 - 12:30" : "Sin Horario");
            map.put("jornada", c.getJornada());
            cursosList.add(map);
        }

        model.addAttribute("cursosList", cursosList);
        model.addAttribute("docentesList", personalService.listarDocentes());
        model.addAttribute("materiasList", cursoService.listarMateriasActivas());
        model.addAttribute("salonesList", cursoService.listarSalonesActivos());
        model.addAttribute("bloquesList", cursoService.listarBloquesPorJornada("Mañana"));
        return "clases/index";
    }

    @PostMapping("/clases/crear")
    public String crearCurso(
            @RequestParam(value = "id", required = false) String idStr,
            @RequestParam("grado") String grado,
            @RequestParam(value = "grupo", required = false) String grupo,
            @RequestParam(value = "jornada", defaultValue = "Mañana") String jornada,
            @RequestParam(value = "cupos", defaultValue = "35") Integer cupos,
            @RequestParam(value = "directorId", required = false) String directorIdStr,
            @RequestParam(value = "anoLectivo", defaultValue = "2026") String anoLectivo,
            RedirectAttributes redirectAttributes) {

        try {
            UUID directorId = (directorIdStr != null && !directorIdStr.isBlank()) ? UUID.fromString(directorIdStr)
                    : null;
            if (idStr != null && !idStr.isBlank()) {
                UUID cursoId = UUID.fromString(idStr);
                cursoService.actualizarCurso(cursoId, grado, jornada, cupos, directorId, anoLectivo);
                redirectAttributes.addFlashAttribute("mensajeExito", "Curso actualizado exitosamente.");
            } else {
                cursoService.crearCurso(grado, grupo, jornada, cupos, directorId, anoLectivo);
                redirectAttributes.addFlashAttribute("mensajeExito", "Curso creado exitosamente.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar curso: " + ex.getMessage());
        }

        return "redirect:/clases";
    }

    @GetMapping("/clases/gestion")
    public String gestionDetail(@RequestParam(name = "codigo", defaultValue = "11-01") String codigo, Model model) {
        model.addAttribute("title", "Gestión de Curso " + codigo + " – IEACI");
        model.addAttribute("activePage", "clases");

        Optional<Docente> docLogueadoOpt = currentUserContextService.getDocenteAutenticado();
        boolean esAdmin = currentUserContextService.esAdmin();

        CursoGestionDetalleDTO detalle = cursoGestionAppService.prepararGestionDetalle(codigo, docLogueadoOpt, esAdmin);

        model.addAttribute("cursoId", detalle.getCursoId());
        model.addAttribute("codigoCurso", detalle.getCodigoCurso());
        model.addAttribute("gradoCurso", detalle.getGradoCurso());
        model.addAttribute("directorCurso", detalle.getDirectorCurso());
        model.addAttribute("jornadaCurso", detalle.getJornadaCurso());
        model.addAttribute("estudiantesData", detalle.getEstudiantesData());
        model.addAttribute("estudiantesMock", detalle.getEstudiantesData());
        model.addAttribute("horariosData", detalle.getHorariosData());
        model.addAttribute("esAdmin", detalle.isEsAdmin());
        model.addAttribute("horarioBannerTexto", detalle.getHorarioBannerTexto());

        CursoTablaNotasDTO tablaDTO = detalle.getTablaNotasDTO();
        model.addAttribute("tieneHorarioHoy", tablaDTO.isTieneHorarioHoy());
        model.addAttribute("periodo", tablaDTO.getPeriodo());
        model.addAttribute("fecha", tablaDTO.getFecha().toString());
        model.addAttribute("estudiantesCE", tablaDTO.getEstudiantesCE());
        model.addAttribute("cursoMateriaId", tablaDTO.getCursoMateriaId());
        model.addAttribute("materiaId", tablaDTO.getMateriaId());
        model.addAttribute("materiaNombre", tablaDTO.getMateriaNombre());
        model.addAttribute("evaluaciones", tablaDTO.getEvaluaciones());
        model.addAttribute("sumaPesos", tablaDTO.getSumaPesos());
        model.addAttribute("calificacionesMapa", tablaDTO.getCalificacionesMapa());
        model.addAttribute("asistenciasMapa", tablaDTO.getAsistenciasMapa());
        model.addAttribute("notasFinales", tablaDTO.getNotasFinales());

        return "clases/detalle";
    }

    @PostMapping("/clases/horarios/guardar")
    public String guardarHorarioCurso(
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam("diaSemana") String diaSemana,
            @RequestParam(value = "materiaId", required = false) String materiaIdStr,
            @RequestParam(value = "docenteId", required = false) String docenteIdStr,
            @RequestParam(value = "horaInicio", defaultValue = "07:00") String horaInicio,
            @RequestParam(value = "horaFin", defaultValue = "08:30") String horaFin,
            @RequestParam(value = "salon", defaultValue = "Aula 101") String salon,
            RedirectAttributes redirectAttributes) {

        try {
            UUID materiaId = (materiaIdStr != null && !materiaIdStr.isBlank()) ? UUID.fromString(materiaIdStr) : null;
            UUID docenteId = (docenteIdStr != null && !docenteIdStr.isBlank()) ? UUID.fromString(docenteIdStr) : null;

            cursoService.guardarHorarioBloque(cursoId, diaSemana, materiaId, docenteId, horaInicio, horaFin, salon);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Horario asignado exitosamente para el día " + diaSemana);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al asignar horario: " + ex.getMessage());
        }

        return "redirect:/clases";
    }

    @PostMapping("/clases/horarios/guardar-grid")
    public String guardarHorarioGrid(
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {

        try {
            int guardados = cursoGestionAppService.procesarGuardadoHorarioGrid(cursoId, allParams);
            if (guardados > 0) {
                redirectAttributes.addFlashAttribute("mensajeExito",
                        "Horario asignado exitosamente (" + guardados + " clases configuradas).");
            } else {
                redirectAttributes.addFlashAttribute("mensajeExito", "Horario del curso actualizado.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar horario: " + ex.getMessage());
        }

        return "redirect:/clases";
    }

    @PostMapping("/clases/guardar")
    public String guardarCurso(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam("grado") String grado,
            @RequestParam(value = "grupo", defaultValue = "01") String grupo,
            @RequestParam("jornada") String jornada,
            @RequestParam(value = "cupos", defaultValue = "35") Integer cupos,
            @RequestParam(value = "directorId", required = false) String directorId,
            RedirectAttributes redirectAttributes) {

        try {
            UUID dirId = (directorId != null && !directorId.isBlank()) ? UUID.fromString(directorId) : null;

            if (id != null && !id.isBlank()) {
                cursoService.actualizarCurso(UUID.fromString(id), grado, jornada, cupos, dirId, "2026");
                redirectAttributes.addFlashAttribute("mensajeExito", "Curso actualizado exitosamente.");
            } else {
                cursoService.crearCurso(grado, grupo, jornada, cupos, dirId, "2026");
                redirectAttributes.addFlashAttribute("mensajeExito", "Curso creado exitosamente.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar el curso: " + ex.getMessage());
        }

        return "redirect:/clases";
    }

    @PostMapping("/clases/eliminar")
    public String eliminarCurso(
            @RequestParam("id") UUID id,
            RedirectAttributes redirectAttributes) {

        try {
            cursoService.eliminarCurso(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Curso eliminado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el curso: " + ex.getMessage());
        }

        return "redirect:/clases";
    }

    @PostMapping("/clases/mapear-estudiantes")
    public String mapearEstudiantes(@RequestParam("cursoId") UUID cursoId, RedirectAttributes redirectAttributes) {
        if (!currentUserContextService.esAdminOAdministrativo()) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Acceso denegado: solo Administradores y Personal Administrativo pueden auto-mapear estudiantes.");
            Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
            String codigo = cOpt.map(Curso::getCodigoCurso).orElse("11-01");
            return "redirect:/clases/gestion?codigo=" + codigo;
        }

        try {
            int count = cursoService.mapearEstudiantesMatriculados(cursoId);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Se han auto-mapeado " + count + " estudiantes matriculados a este curso.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al mapear estudiantes: " + ex.getMessage());
        }

        Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
        String codigo = cOpt.map(Curso::getCodigoCurso).orElse("11-01");
        return "redirect:/clases/gestion?codigo=" + codigo;
    }

    @PostMapping("/clases/promover-estudiantes")
    public String promoverEstudiantes(@RequestParam("cursoId") UUID cursoId,
            @RequestParam(value = "notasJson", required = false) String notasJson,
            RedirectAttributes redirectAttributes) {
        if (!currentUserContextService.esAdminOAdministrativo()) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Acceso denegado: solo Administradores y Personal Administrativo pueden promover estudiantes.");
            Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
            String codigo = cOpt.map(Curso::getCodigoCurso).orElse("11-01");
            return "redirect:/clases/gestion?codigo=" + codigo;
        }

        try {
            String resultado = cursoService.promoverEstudiantesAprobados(cursoId, notasJson);
            redirectAttributes.addFlashAttribute("mensajeExito", resultado);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al promover estudiantes: " + ex.getMessage());
        }

        Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
        String codigo = cOpt.map(Curso::getCodigoCurso).orElse("11-01");
        return "redirect:/clases/gestion?codigo=" + codigo;
    }

    // ==========================================
    // HTMX ENDPOINTS: NOTAS Y ASISTENCIA (POSTGRESQL)
    // ==========================================

    @GetMapping("/clases/fragmento/tabla-notas")
    public String obtenerFragmentoTablaNotas(
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam(value = "materiaNombre", required = false) String materiaNombre,
            @RequestParam(value = "materiaId", required = false) UUID materiaId,
            @RequestParam(value = "periodo", defaultValue = "1") Integer periodo,
            @RequestParam(value = "fecha", required = false) String fechaStr,
            Model model) {

        LocalDate fecha;
        try {
            fecha = (fechaStr != null && !fechaStr.isBlank()) ? LocalDate.parse(fechaStr) : LocalDate.now();
        } catch (Exception e) {
            fecha = LocalDate.now();
        }

        Optional<Docente> docLogueadoOpt = currentUserContextService.getDocenteAutenticado();
        boolean esAdmin = currentUserContextService.esAdmin();

        CursoTablaNotasDTO dto = cursoGestionAppService.prepararTablaNotas(
                cursoId, materiaNombre, materiaId, periodo, fecha, docLogueadoOpt, esAdmin);

        model.addAttribute("cursoId", dto.getCursoId());
        model.addAttribute("periodo", dto.getPeriodo());
        model.addAttribute("fecha", dto.getFecha().toString());
        model.addAttribute("diaSemana", dto.getDiaSemana());
        model.addAttribute("tieneHorarioHoy", dto.isTieneHorarioHoy());
        model.addAttribute("horarioBannerTexto", dto.getHorarioBannerTexto());
        model.addAttribute("materiaNombre", dto.getMateriaNombre());
        model.addAttribute("materiaId", dto.getMateriaId());
        model.addAttribute("cursoMateriaId", dto.getCursoMateriaId());
        model.addAttribute("estudiantesCE", dto.getEstudiantesCE());
        model.addAttribute("evaluaciones", dto.getEvaluaciones());
        model.addAttribute("sumaPesos", dto.getSumaPesos());
        model.addAttribute("calificacionesMapa", dto.getCalificacionesMapa());
        model.addAttribute("asistenciasMapa", dto.getAsistenciasMapa());
        model.addAttribute("notasFinales", dto.getNotasFinales());

        return "clases/fragments/tabla-detalle-notas :: tablaDetalleNotas";
    }

    @PostMapping("/clases/evaluaciones/crear")
    public String crearEvaluacion(
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam(value = "materiaNombre", required = false) String materiaNombre,
            @RequestParam(value = "materiaId", required = false) UUID materiaId,
            @RequestParam(value = "periodo", defaultValue = "1") Integer periodo,
            @RequestParam(value = "fecha", required = false) String fecha,
            Model model) {

        if ((materiaNombre == null || materiaNombre.isBlank()) && materiaId == null) {
            return obtenerFragmentoTablaNotas(cursoId, materiaNombre, materiaId, periodo, fecha, model);
        }

        CursoMateria cm = (materiaId != null)
                ? calificacionesService.obtenerOCrearCursoMateria(cursoId, materiaId, "2026")
                : calificacionesService.obtenerOCrearCursoMateriaPorNombre(cursoId, materiaNombre, "2026");

        calificacionesService.crearEvaluacionAutoEquitativa(cm.getId(), periodo, fecha);

        return obtenerFragmentoTablaNotas(cursoId, materiaNombre, cm.getMateria().getId(), periodo, fecha, model);
    }

    @PostMapping("/clases/evaluaciones/eliminar")
    public String eliminarEvaluacion(
            @RequestParam("evaluacionId") UUID evaluacionId,
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam(value = "materiaNombre", required = false) String materiaNombre,
            @RequestParam(value = "materiaId", required = false) UUID materiaId,
            @RequestParam(value = "periodo", defaultValue = "1") Integer periodo,
            @RequestParam(value = "fecha", required = false) String fecha,
            Model model) {

        calificacionesService.eliminarEvaluacion(evaluacionId);

        return obtenerFragmentoTablaNotas(cursoId, materiaNombre, materiaId, periodo, fecha, model);
    }

    @PostMapping("/clases/evaluaciones/actualizar-peso")
    public String actualizarPeso(
            @RequestParam("evaluacionId") UUID evaluacionId,
            @RequestParam("peso") String pesoStr,
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam(value = "materiaNombre", required = false) String materiaNombre,
            @RequestParam(value = "materiaId", required = false) UUID materiaId,
            @RequestParam(value = "periodo", defaultValue = "1") Integer periodo,
            @RequestParam(value = "fecha", required = false) String fecha,
            Model model) {

        BigDecimal peso = BigDecimal.ZERO;
        if (pesoStr != null && !pesoStr.isBlank()) {
            try {
                peso = new BigDecimal(pesoStr.trim().replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
            } catch (Exception ignored) {
            }
        }

        calificacionesService.actualizarPesoEvaluacion(evaluacionId, peso);

        return obtenerFragmentoTablaNotas(cursoId, materiaNombre, materiaId, periodo, fecha, model);
    }

    @PostMapping("/clases/calificaciones/guardar")
    public String guardarCalificacion(
            @RequestParam("evaluacionId") UUID evaluacionId,
            @RequestParam("estudianteId") UUID estudianteId,
            @RequestParam("nota") String notaStr,
            @RequestParam("cursoMateriaId") UUID cursoMateriaId,
            @RequestParam("periodo") Integer periodo,
            Model model) {

        BigDecimal nota = BigDecimal.ZERO;
        if (notaStr != null && !notaStr.isBlank()) {
            try {
                nota = new BigDecimal(notaStr.trim().replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
            } catch (Exception ignored) {
            }
        }

        calificacionesService.registrarONota(evaluacionId, estudianteId, nota, null);
        BigDecimal notaFinal = calificacionesService.calcularNotaFinalPeriodo(estudianteId, cursoMateriaId, periodo);

        model.addAttribute("estId", estudianteId.toString());
        model.addAttribute("notaFinal", notaFinal);

        return "clases/fragments/tabla-detalle-notas :: badgeNotaFinal";
    }

    @PostMapping("/clases/asistencias/toggle")
    public String toggleAsistencia(
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam("estudianteId") UUID estudianteId,
            @RequestParam(value = "materiaId", required = false) UUID materiaId,
            @RequestParam("fecha") String fechaStr,
            Model model) {

        LocalDate fecha = (fechaStr != null && !fechaStr.isBlank()) ? LocalDate.parse(fechaStr) : LocalDate.now();
        Asistencia a = asistenciaService.toggleAsistencia(cursoId, estudianteId, fecha, materiaId);

        model.addAttribute("estId", estudianteId.toString());
        model.addAttribute("cursoId", cursoId.toString());
        model.addAttribute("materiaId", materiaId != null ? materiaId.toString() : "");
        model.addAttribute("fecha", fechaStr);
        model.addAttribute("estado", a.getEstado());
        model.addAttribute("tieneHorarioHoy", true);

        return "clases/fragments/tabla-detalle-notas :: botonAsistencia";
    }
}
