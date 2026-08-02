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

        // Fallback sample data if DB has no courses yet
        if (cursosList.isEmpty()) {
            cursosList.add(createCurso("11°", "01", "Jorge Eliécer Rojas", 30, 28, true, "Lun-Vie: 07:00 - 12:30", "Mañana"));
            cursosList.add(createCurso("11°", "02", "Martha Cecilia Ruiz", 30, 25, false, "Sin Horario", "Mañana"));
            cursosList.add(createCurso("10°", "01", "Ana María Sánchez", 35, 32, true, "Lun-Vie: 07:00 - 12:30", "Mañana"));
            cursosList.add(createCurso("9°", "01", "Carlos Mendoza", 35, 26, true, "Lun-Vie: 07:00 - 12:30", "Mañana"));
            cursosList.add(createCurso("8°", "01", "Luis Felipe Gómez", 35, 30, false, "Sin Horario", "Tarde"));
            cursosList.add(createCurso("6°", "01", "Patricia López", 35, 15, false, "Sin Horario", "Tarde"));
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

            if (estudiantesMock.isEmpty()) {
                estudiantesMock = cargarEstudiantesEjemplo();
            }
            model.addAttribute("estudiantesMock", estudiantesMock);
        } else {
            model.addAttribute("codigoCurso", codigo);
            model.addAttribute("gradoCurso", codigo.split("-")[0] + "°");
            model.addAttribute("directorCurso", "Jorge Eliécer Rojas");
            model.addAttribute("jornadaCurso", "Mañana");
            model.addAttribute("estudiantesMock", cargarEstudiantesEjemplo());
        }

        return "clases/detalle";
    }

    private List<Map<String, Object>> cargarEstudiantesEjemplo() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createEstudiante(1, "Álvarez Restrepo, Mateo", "1098432101", "Presente"));
        list.add(createEstudiante(2, "Bermúdez Castro, Sofia", "1098432102", "Presente"));
        list.add(createEstudiante(3, "Cárdenas Morales, Juan Diego", "1098432103", "No presente"));
        list.add(createEstudiante(4, "Díaz Gómez, Valentina", "1098432104", "Presente"));
        list.add(createEstudiante(5, "Espinosa Vargas, Andrés Felipe", "1098432105", "Excusado"));
        list.add(createEstudiante(6, "Franco Gutiérrez, Isabella", "1098432106", "Presente"));
        list.add(createEstudiante(7, "Gómez Hernández, Santiago", "1098432107", "No presente"));
        return list;
    }

    private Map<String, Object> createCurso(String grado, String grupo, String director, int cupos, int estudiantes, boolean tieneHorario, String horarioResumen, String jornada) {
        Map<String, Object> c = new HashMap<>();
        c.put("grado", grado);
        c.put("grupo", grupo);
        c.put("codigoCurso", grado + "-" + grupo);
        c.put("director", director);
        c.put("cuposMaximos", cupos);
        c.put("estudiantes", estudiantes);
        c.put("tieneHorario", tieneHorario);
        c.put("horarioResumen", horarioResumen);
        c.put("jornada", jornada);
        return c;
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
