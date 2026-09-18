package com.siga.siga_iea.clases.controller;

import com.siga.siga_iea.clases.entity.Clase;
import com.siga.siga_iea.clases.entity.CursoEstudiante;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.clases.service.ClaseService;
import com.siga.siga_iea.usuarios.service.PersonalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.service.AsistenciaService;
import com.siga.siga_iea.calificaciones.entity.Evaluacion;
import com.siga.siga_iea.calificaciones.service.CalificacionesService;
import com.siga.siga_iea.clases.entity.CursoMateria;
import com.siga.siga_iea.clases.entity.Materia;
import com.siga.siga_iea.clases.repository.MateriaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Controller
public class ClaseController {

    private final ClaseService claseService;
    private final PersonalService personalService;
    private final CalificacionesService calificacionesService;
    private final AsistenciaService asistenciaService;
    private final MateriaRepository materiaRepository;

    public ClaseController(ClaseService claseService,
                           PersonalService personalService,
                           CalificacionesService calificacionesService,
                           AsistenciaService asistenciaService,
                           MateriaRepository materiaRepository) {
        this.claseService = claseService;
        this.personalService = personalService;
        this.calificacionesService = calificacionesService;
        this.asistenciaService = asistenciaService;
        this.materiaRepository = materiaRepository;
    }

    public static String obtenerNombreDiaEspanol(DayOfWeek dow) {
        if (dow == null) return "Lunes";
        switch (dow) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: return "Lunes";
        }
    }

    @GetMapping("/clases/horarios/datos")
    @ResponseBody
    public List<Map<String, Object>> obtenerHorariosDatosJson(@RequestParam("cursoId") UUID cursoId) {
        List<Horario> horarios = claseService.listarHorariosDeCurso(cursoId);
        List<Map<String, Object>> res = new ArrayList<>();
        for (Horario h : horarios) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", h.getId().toString());
            map.put("dia", h.getDiaSemana());
            map.put("horaInicio", h.getHoraInicio() != null ? h.getHoraInicio().toString() : "07:00");
            map.put("horaFin", h.getHoraFin() != null ? h.getHoraFin().toString() : "08:30");
            map.put("materiaId", h.getMateria() != null ? h.getMateria().getId().toString() : "");
            map.put("materiaNombre", h.getMateria() != null ? h.getMateria().getNombre() : "");
            map.put("docenteId", h.getDocente() != null ? h.getDocente().getId().toString() : "");
            map.put("docenteNombre", h.getDocente() != null ? h.getDocente().getNombreCompleto() : "");
            map.put("salon", h.getSalon() != null ? h.getSalon() : "Aula 101");
            res.add(map);
        }
        return res;
    }

    @GetMapping("/clases")
    public String index(Model model) {
        model.addAttribute("title", "Gestión de Cursos – IEACI");
        model.addAttribute("activePage", "clases");

        List<Clase> cursosDB = claseService.listarCursosPorAno("2026");
        List<Map<String, Object>> cursosList = new ArrayList<>();

        for (Clase c : cursosDB) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId().toString());
            map.put("grado", c.getGrado());
            map.put("grupo", c.getGrupo());
            map.put("codigoCurso", c.getCodigoCurso());
            map.put("director", c.getDirector() != null ? c.getDirector().getNombreCompleto() : "Sin asignar");
            map.put("directorId", c.getDirector() != null ? c.getDirector().getId().toString() : "");
            map.put("cuposMaximos", c.getCuposMaximos());

            List<CursoEstudiante> estudiantes = claseService.listarEstudiantesDeCurso(c.getId());
            map.put("estudiantes", estudiantes.size());

            List<Horario> horarios = claseService.listarHorariosDeCurso(c.getId());
            boolean tieneHorario = !horarios.isEmpty();
            map.put("tieneHorario", tieneHorario);
            map.put("horarioResumen", tieneHorario ? "Lun-Vie: 07:00 - 12:30" : "Sin Horario");
            map.put("jornada", c.getJornada());
            cursosList.add(map);
        }

        model.addAttribute("cursosList", cursosList);
        model.addAttribute("docentesList", personalService.listarDocentes());
        model.addAttribute("materiasList", claseService.listarTodasMaterias());
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
            UUID directorId = (directorIdStr != null && !directorIdStr.isBlank()) ? UUID.fromString(directorIdStr) : null;
            if (idStr != null && !idStr.isBlank()) {
                UUID cursoId = UUID.fromString(idStr);
                claseService.actualizarCurso(cursoId, grado, jornada, cupos, directorId, anoLectivo);
                redirectAttributes.addFlashAttribute("mensajeExito", "Curso actualizado exitosamente.");
            } else {
                claseService.crearCurso(grado, grupo, jornada, cupos, directorId, anoLectivo);
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

        Optional<Clase> claseOpt = claseService.buscarPorCodigo(codigo, "2026");
        Clase c;
        if (claseOpt.isPresent()) {
            c = claseOpt.get();
        } else {
            String degree = codigo.contains("-") ? codigo.split("-")[0] + "°" : codigo;
            String group = codigo.contains("-") ? codigo.split("-")[1] : "01";
            c = claseService.crearCurso(degree, group, "Mañana", 35, null, "2026");
        }

        model.addAttribute("cursoId", c.getId().toString());
        model.addAttribute("codigoCurso", c.getCodigoCurso());
        model.addAttribute("gradoCurso", c.getGrado());
        model.addAttribute("directorCurso", c.getDirector() != null ? c.getDirector().getNombreCompleto() : "Sin asignar");
        model.addAttribute("jornadaCurso", c.getJornada());

        List<CursoEstudiante> estudiantesCE = claseService.listarEstudiantesDeCurso(c.getId());
        List<Map<String, Object>> estudiantesData = new ArrayList<>();
        int idx = 1;
        for (CursoEstudiante ce : estudiantesCE) {
            if (ce.getEstudiante() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ce.getEstudiante().getId().toString());
                map.put("numIdx", idx++);
                map.put("nombre", ce.getEstudiante().getNombreCompleto());
                map.put("documento", ce.getEstudiante().getNumeroDocumento());
                map.put("asistencia", "Presente");
                estudiantesData.add(map);
            }
        }
        model.addAttribute("estudiantesData", estudiantesData);
        model.addAttribute("estudiantesMock", estudiantesData);

        List<Horario> horarios = claseService.listarHorariosDeCurso(c.getId());
        List<Map<String, String>> horariosData = new ArrayList<>();
        if (!horarios.isEmpty()) {
            for (Horario h : horarios) {
                Map<String, String> hm = new HashMap<>();
                hm.put("dia", h.getDiaSemana());
                hm.put("materia", h.getMateria() != null ? h.getMateria().getNombre() : "");
                hm.put("docente", h.getDocente() != null ? h.getDocente().getNombreCompleto() : "Sin docente asignado");
                hm.put("hora", (h.getHoraInicio() != null ? h.getHoraInicio().toString() : "07:00") + " - " + (h.getHoraFin() != null ? h.getHoraFin().toString() : "08:30"));
                horariosData.add(hm);
            }
        }
        model.addAttribute("horariosData", horariosData);

        LocalDate hoy = LocalDate.now();
        String diaHoy = obtenerNombreDiaEspanol(hoy.getDayOfWeek());

        List<Horario> horariosDelDia = horarios.stream()
                .filter(h -> h.getDiaSemana() != null && h.getDiaSemana().equalsIgnoreCase(diaHoy))
                .toList();
        boolean tieneHorarioHoy = !horariosDelDia.isEmpty();
        model.addAttribute("tieneHorarioHoy", tieneHorarioHoy);

        Integer periodo = 1;
        model.addAttribute("periodo", periodo);
        model.addAttribute("fecha", hoy.toString());
        model.addAttribute("estudiantesCE", estudiantesCE);

        if (tieneHorarioHoy) {
            Horario primerH = horariosDelDia.get(0);
            String materiaNombre = primerH.getMateria() != null ? primerH.getMateria().getNombre() : "";
            UUID materiaId = primerH.getMateria() != null ? primerH.getMateria().getId() : null;

            CursoMateria cm = (materiaId != null)
                    ? calificacionesService.obtenerOCrearCursoMateria(c.getId(), materiaId, "2026")
                    : calificacionesService.obtenerOCrearCursoMateriaPorNombre(c.getId(), materiaNombre, "2026");

            List<Evaluacion> evaluaciones = calificacionesService.obtenerEvaluacionesPorCursoYMateria(cm.getId(), periodo);
            BigDecimal sumaPesos = evaluaciones.stream()
                    .map(e -> e.getPeso() != null ? e.getPeso() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<UUID, Map<UUID, BigDecimal>> calificacionesMapa = calificacionesService.obtenerCalificacionesMapa(cm.getId(), periodo);
            Map<UUID, String> asistenciasMapa = asistenciaService.obtenerMapaEstadosAsistencia(c.getId(), hoy, cm.getMateria().getId());

            Map<UUID, BigDecimal> notasFinales = new HashMap<>();
            for (CursoEstudiante ce : estudiantesCE) {
                if (ce.getEstudiante() != null) {
                    UUID estId = ce.getEstudiante().getId();
                    BigDecimal notaFinal = calificacionesService.calcularNotaFinalPeriodo(estId, cm.getId(), periodo);
                    notasFinales.put(estId, notaFinal);
                }
            }

            model.addAttribute("cursoMateriaId", cm.getId());
            model.addAttribute("materiaId", cm.getMateria().getId());
            model.addAttribute("materiaNombre", cm.getMateria().getNombre());
            model.addAttribute("evaluaciones", evaluaciones);
            model.addAttribute("sumaPesos", sumaPesos);
            model.addAttribute("calificacionesMapa", calificacionesMapa);
            model.addAttribute("asistenciasMapa", asistenciasMapa);
            model.addAttribute("notasFinales", notasFinales);
        } else {
            model.addAttribute("cursoMateriaId", null);
            model.addAttribute("materiaId", null);
            model.addAttribute("materiaNombre", "");
            model.addAttribute("evaluaciones", Collections.emptyList());
            model.addAttribute("sumaPesos", BigDecimal.ZERO);
            model.addAttribute("calificacionesMapa", Collections.emptyMap());
            model.addAttribute("asistenciasMapa", Collections.emptyMap());
            model.addAttribute("notasFinales", Collections.emptyMap());
        }

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

            claseService.guardarHorarioBloque(cursoId, diaSemana, materiaId, docenteId, horaInicio, horaFin, salon);
            redirectAttributes.addFlashAttribute("mensajeExito", "Horario asignado exitosamente para el día " + diaSemana);
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
            claseService.limpiarHorarioCurso(cursoId);

            String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
            int guardados = 0;

            Set<Integer> slotIndices = new TreeSet<>();
            for (String key : allParams.keySet()) {
                if (key.startsWith("slot_") && key.endsWith("_inicio")) {
                    try {
                        String idxStr = key.substring(5, key.indexOf("_inicio"));
                        slotIndices.add(Integer.parseInt(idxStr));
                    } catch (Exception ignored) {}
                }
            }

            for (Integer i : slotIndices) {
                String horaInicio = allParams.get("slot_" + i + "_inicio");
                String horaFin = allParams.get("slot_" + i + "_fin");
                String salon = allParams.getOrDefault("slot_" + i + "_salon", "Aula 101");

                if (horaInicio == null || horaFin == null || horaInicio.isBlank() || horaFin.isBlank()) continue;

                for (String dia : dias) {
                    String matKey = "slot_" + i + "_" + dia + "_materiaId";
                    String docKey = "slot_" + i + "_" + dia + "_docenteId";

                    String matIdStr = allParams.get(matKey);
                    String docIdStr = allParams.get(docKey);

                    if (matIdStr == null || matIdStr.isBlank()) {
                        String diaAlt = dia.contains("é") ? dia.replace("é", "e") : dia.replace("e", "é");
                        String matKeyAlt = "slot_" + i + "_" + diaAlt + "_materiaId";
                        String docKeyAlt = "slot_" + i + "_" + diaAlt + "_docenteId";
                        if (allParams.containsKey(matKeyAlt)) {
                            matIdStr = allParams.get(matKeyAlt);
                            docIdStr = allParams.get(docKeyAlt);
                        }
                    }

                    if (matIdStr != null && !matIdStr.isBlank()) {
                        UUID materiaId = UUID.fromString(matIdStr);
                        UUID docenteId = (docIdStr != null && !docIdStr.isBlank()) ? UUID.fromString(docIdStr) : null;

                        claseService.guardarHorarioBloque(cursoId, dia, materiaId, docenteId, horaInicio, horaFin, salon);
                        guardados++;
                    }
                }
            }

            if (guardados > 0) {
                redirectAttributes.addFlashAttribute("mensajeExito", "Horario asignado exitosamente (" + guardados + " clases configuradas).");
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
                claseService.actualizarCurso(UUID.fromString(id), grado, jornada, cupos, dirId, "2026");
                redirectAttributes.addFlashAttribute("mensajeExito", "Curso actualizado exitosamente.");
            } else {
                claseService.crearCurso(grado, grupo, jornada, cupos, dirId, "2026");
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
            claseService.eliminarCurso(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Curso eliminado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el curso: " + ex.getMessage());
        }

        return "redirect:/clases";
    }

    @PostMapping("/clases/mapear-estudiantes")
    public String mapearEstudiantes(@RequestParam("cursoId") UUID cursoId, RedirectAttributes redirectAttributes) {
        try {
            int count = claseService.mapearEstudiantesMatriculados(cursoId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Se han auto-mapeado " + count + " estudiantes matriculados a este curso.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al mapear estudiantes: " + ex.getMessage());
        }

        Optional<Clase> cOpt = claseService.buscarPorId(cursoId);
        String codigo = cOpt.map(Clase::getCodigoCurso).orElse("11-01");
        return "redirect:/clases/gestion?codigo=" + codigo;
    }

    @PostMapping("/clases/promover-estudiantes")
    public String promoverEstudiantes(@RequestParam("cursoId") UUID cursoId,
                                      @RequestParam(value = "notasJson", required = false) String notasJson,
                                      RedirectAttributes redirectAttributes) {
        try {
            String resultado = claseService.promoverEstudiantesAprobados(cursoId, notasJson);
            redirectAttributes.addFlashAttribute("mensajeExito", resultado);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al promover estudiantes: " + ex.getMessage());
        }

        Optional<Clase> cOpt = claseService.buscarPorId(cursoId);
        String codigo = cOpt.map(Clase::getCodigoCurso).orElse("11-01");
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

        String diaSemana = obtenerNombreDiaEspanol(fecha.getDayOfWeek());
        List<Horario> horarios = claseService.listarHorariosDeCurso(cursoId);
        List<Horario> horariosDelDia = horarios.stream()
                .filter(h -> h.getDiaSemana() != null && h.getDiaSemana().equalsIgnoreCase(diaSemana))
                .toList();

        List<CursoEstudiante> estudiantesCE = claseService.listarEstudiantesDeCurso(cursoId);
        model.addAttribute("estudiantesCE", estudiantesCE);
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("periodo", periodo);
        model.addAttribute("fecha", fecha.toString());

        // Si no hay horario configurado para este día de la semana
        if (horariosDelDia.isEmpty() || (materiaNombre != null && materiaNombre.isBlank() && materiaId == null)) {
            model.addAttribute("tieneHorarioHoy", false);
            model.addAttribute("materiaNombre", "");
            model.addAttribute("materiaId", null);
            model.addAttribute("cursoMateriaId", null);
            model.addAttribute("evaluaciones", Collections.emptyList());
            model.addAttribute("sumaPesos", BigDecimal.ZERO);
            model.addAttribute("calificacionesMapa", Collections.emptyMap());
            model.addAttribute("asistenciasMapa", Collections.emptyMap());
            model.addAttribute("notasFinales", Collections.emptyMap());
            return "clases/fragments/tabla-detalle-notas :: tablaDetalleNotas";
        }

        // Determinar qué horario/materia de las programadas para hoy se está consultando
        final UUID targetMateriaId = materiaId;
        final String targetMateriaNombre = materiaNombre;
        Optional<Horario> horarioMatch = Optional.empty();
        if (targetMateriaId != null) {
            horarioMatch = horariosDelDia.stream()
                    .filter(h -> h.getMateria() != null && h.getMateria().getId().equals(targetMateriaId))
                    .findFirst();
        } else if (targetMateriaNombre != null && !targetMateriaNombre.isBlank()) {
            horarioMatch = horariosDelDia.stream()
                    .filter(h -> h.getMateria() != null && h.getMateria().getNombre().equalsIgnoreCase(targetMateriaNombre))
                    .findFirst();
        }

        Horario hSeleccionado = horarioMatch.orElse(horariosDelDia.get(0));
        String selectedMateriaNombre = hSeleccionado.getMateria() != null ? hSeleccionado.getMateria().getNombre() : "";
        UUID selectedMateriaId = hSeleccionado.getMateria() != null ? hSeleccionado.getMateria().getId() : null;

        if (selectedMateriaId == null && selectedMateriaNombre.isBlank()) {
            model.addAttribute("tieneHorarioHoy", false);
            model.addAttribute("materiaNombre", "");
            model.addAttribute("materiaId", null);
            model.addAttribute("cursoMateriaId", null);
            model.addAttribute("evaluaciones", Collections.emptyList());
            model.addAttribute("sumaPesos", BigDecimal.ZERO);
            model.addAttribute("calificacionesMapa", Collections.emptyMap());
            model.addAttribute("asistenciasMapa", Collections.emptyMap());
            model.addAttribute("notasFinales", Collections.emptyMap());
            return "clases/fragments/tabla-detalle-notas :: tablaDetalleNotas";
        }

        model.addAttribute("tieneHorarioHoy", true);

        CursoMateria cm = (selectedMateriaId != null)
                ? calificacionesService.obtenerOCrearCursoMateria(cursoId, selectedMateriaId, "2026")
                : calificacionesService.obtenerOCrearCursoMateriaPorNombre(cursoId, selectedMateriaNombre, "2026");

        List<Evaluacion> evaluaciones = calificacionesService.obtenerEvaluacionesPorCursoYMateria(cm.getId(), periodo);
        BigDecimal sumaPesos = evaluaciones.stream()
                .map(e -> e.getPeso() != null ? e.getPeso() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, Map<UUID, BigDecimal>> calificacionesMapa = calificacionesService.obtenerCalificacionesMapa(cm.getId(), periodo);
        Map<UUID, String> asistenciasMapa = asistenciaService.obtenerMapaEstadosAsistencia(cursoId, fecha, cm.getMateria().getId());

        Map<UUID, BigDecimal> notasFinales = new HashMap<>();
        for (CursoEstudiante ce : estudiantesCE) {
            if (ce.getEstudiante() != null) {
                UUID estId = ce.getEstudiante().getId();
                BigDecimal notaFinal = calificacionesService.calcularNotaFinalPeriodo(estId, cm.getId(), periodo);
                notasFinales.put(estId, notaFinal);
            }
        }

        model.addAttribute("cursoMateriaId", cm.getId());
        model.addAttribute("materiaId", cm.getMateria().getId());
        model.addAttribute("materiaNombre", cm.getMateria().getNombre());
        model.addAttribute("evaluaciones", evaluaciones);
        model.addAttribute("sumaPesos", sumaPesos);
        model.addAttribute("calificacionesMapa", calificacionesMapa);
        model.addAttribute("asistenciasMapa", asistenciasMapa);
        model.addAttribute("notasFinales", notasFinales);

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
            } catch (Exception ignored) {}
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
            } catch (Exception ignored) {}
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
