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

import java.time.LocalTime;
import java.util.*;

@Controller
public class ClaseController {

    private final ClaseService claseService;
    private final PersonalService personalService;

    public ClaseController(ClaseService claseService, PersonalService personalService) {
        this.claseService = claseService;
        this.personalService = personalService;
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
                hm.put("materia", h.getMateria() != null ? h.getMateria().getNombre() : "Matemáticas");
                hm.put("docente", h.getDocente() != null ? h.getDocente().getNombreCompleto() : "Sin docente asignado");
                hm.put("hora", (h.getHoraInicio() != null ? h.getHoraInicio().toString() : "07:00") + " - " + (h.getHoraFin() != null ? h.getHoraFin().toString() : "08:30"));
                horariosData.add(hm);
            }
        }
        model.addAttribute("horariosData", horariosData);

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

    @PostMapping("/clases/eliminar")
    public String eliminarCurso(@RequestParam("cursoId") UUID cursoId, RedirectAttributes redirectAttributes) {
        try {
            claseService.eliminarCurso(cursoId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Curso eliminado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar curso: " + ex.getMessage());
        }
        return "redirect:/clases";
    }

    @PostMapping("/clases/mapear-estudiantes")
    public String mapearEstudiantes(@RequestParam("cursoId") UUID cursoId, RedirectAttributes redirectAttributes) {
        try {
            int asignados = claseService.mapearEstudiantesMatriculados(cursoId);
            if (asignados > 0) {
                redirectAttributes.addFlashAttribute("mensajeExito", "Se mapearon y asignaron " + asignados + " estudiantes matriculados al curso.");
            } else {
                redirectAttributes.addFlashAttribute("mensajeError", "No se encontraron estudiantes matriculados pendientes por asignar en este grado.");
            }
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
}
