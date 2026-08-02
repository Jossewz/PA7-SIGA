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
        return "clases/index";
    }

    @PostMapping("/clases/crear")
    public String crearCurso(
            @RequestParam("grado") String grado,
            @RequestParam(value = "grupo", defaultValue = "01") String grupo,
            @RequestParam(value = "jornada", defaultValue = "Mañana") String jornada,
            @RequestParam(value = "cupos", defaultValue = "35") Integer cupos,
            @RequestParam(value = "directorId", required = false) UUID directorId,
            RedirectAttributes redirectAttributes) {

        try {
            claseService.crearCurso(grado, grupo, jornada, cupos, directorId, "2026");
            redirectAttributes.addFlashAttribute("mensajeExito", "Curso creado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear curso: " + ex.getMessage());
        }

        return "redirect:/clases";
    }

    @GetMapping("/clases/gestion")
    public String gestionDetail(@RequestParam(name = "codigo", defaultValue = "11-01") String codigo, Model model) {
        model.addAttribute("title", "Gestión de Curso " + codigo + " – IEACI");
        model.addAttribute("activePage", "clases");

        Optional<Clase> claseOpt = claseService.buscarPorCodigo(codigo, "2026");

        if (claseOpt.isPresent()) {
            Clase c = claseOpt.get();
            model.addAttribute("codigoCurso", c.getCodigoCurso());
            model.addAttribute("gradoCurso", c.getGrado());
            model.addAttribute("directorCurso", c.getDirector() != null ? c.getDirector().getNombreCompleto() : "Sin asignar");
            model.addAttribute("jornadaCurso", c.getJornada());

            List<CursoEstudiante> estudiantesCE = claseService.listarEstudiantesDeCurso(c.getId());
            List<Map<String, Object>> estudiantesMock = new ArrayList<>();
            int idx = 1;
            for (CursoEstudiante ce : estudiantesCE) {
                estudiantesMock.add(createEstudiante(idx++, ce.getEstudiante().getNombreCompleto(), ce.getEstudiante().getNumeroDocumento(), "Presente"));
            }
            model.addAttribute("estudiantesMock", estudiantesMock);
        } else {
            model.addAttribute("codigoCurso", codigo);
            model.addAttribute("gradoCurso", codigo.contains("-") ? codigo.split("-")[0] + "°" : codigo);
            model.addAttribute("directorCurso", "Sin asignar");
            model.addAttribute("jornadaCurso", "Mañana");
            model.addAttribute("estudiantesMock", Collections.emptyList());
        }

        return "clases/detalle";
    }

    private Map<String, Object> createEstudiante(int id, String nombre, String documento, String asistencia) {
        Map<String, Object> e = new HashMap<>();
        e.put("id", id);
        e.put("nombre", nombre);
        e.put("documento", documento);
        e.put("asistencia", asistencia);
        return e;
    }
}
