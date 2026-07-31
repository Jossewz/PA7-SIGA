package com.siga.siga_iea.clases.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ClaseController {

    @GetMapping("/clases")
    public String index(Model model) {
        model.addAttribute("title", "Gestión de Cursos – IEACI");
        model.addAttribute("activePage", "clases");

        // Mock list of courses with numeric sequences and schedule status
        List<Map<String, Object>> cursosList = new ArrayList<>();
        cursosList.add(createCurso("11°", "01", "Jorge Eliécer Rojas", 30, 28, true, "Lun-Vie: 07:00 - 12:30", "Mañana"));
        cursosList.add(createCurso("11°", "02", "Martha Cecilia Ruiz", 30, 25, false, "Sin Horario", "Mañana"));
        cursosList.add(createCurso("10°", "01", "Ana María Sánchez", 35, 32, true, "Lun-Vie: 07:00 - 12:30", "Mañana"));
        cursosList.add(createCurso("9°", "01", "Carlos Mendoza", 35, 26, true, "Lun-Vie: 07:00 - 12:30", "Mañana"));
        cursosList.add(createCurso("8°", "01", "Luis Felipe Gómez", 35, 30, false, "Sin Horario", "Tarde"));
        cursosList.add(createCurso("6°", "01", "Patricia López", 35, 15, false, "Sin Horario", "Tarde"));

        model.addAttribute("cursosList", cursosList);
        return "clases/index";
    }

    @GetMapping("/clases/gestion")
    public String gestionDetail(@RequestParam(name = "codigo", defaultValue = "11-01") String codigo, Model model) {
        model.addAttribute("title", "Gestión de Curso " + codigo + " – IEACI");
        model.addAttribute("activePage", "clases");
        model.addAttribute("codigoCurso", codigo);
        model.addAttribute("gradoCurso", codigo.split("-")[0] + "°");
        model.addAttribute("directorCurso", "Jorge Eliécer Rojas");
        model.addAttribute("jornadaCurso", "Mañana");

        // Mock list of students for detail view
        List<Map<String, Object>> estudiantesMock = new ArrayList<>();
        estudiantesMock.add(createEstudiante(1, "Álvarez Restrepo, Mateo", "1098432101", "Presente"));
        estudiantesMock.add(createEstudiante(2, "Bermúdez Castro, Sofia", "1098432102", "Presente"));
        estudiantesMock.add(createEstudiante(3, "Cárdenas Morales, Juan Diego", "1098432103", "No presente"));
        estudiantesMock.add(createEstudiante(4, "Díaz Gómez, Valentina", "1098432104", "Presente"));
        estudiantesMock.add(createEstudiante(5, "Espinosa Vargas, Andrés Felipe", "1098432105", "Excusado"));
        estudiantesMock.add(createEstudiante(6, "Franco Gutiérrez, Isabella", "1098432106", "Presente"));
        estudiantesMock.add(createEstudiante(7, "Gómez Hernández, Santiago", "1098432107", "No presente"));

        model.addAttribute("estudiantesMock", estudiantesMock);
        return "clases/detalle";
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
