package com.siga.siga_iea.clases.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

        // Mock list of courses
        List<Map<String, Object>> cursosList = new ArrayList<>();
        cursosList.add(createCurso("9°", "A", "Carlos Mendoza", 35, 26, "Activo"));
        cursosList.add(createCurso("10°", "A", "Ana María Sánchez", 35, 32, "Activo"));
        cursosList.add(createCurso("11°", "A", "Jorge Eliécer Rojas", 30, 28, "Activo"));
        cursosList.add(createCurso("8°", "B", "Luis Felipe Gómez", 35, 30, "Activo"));
        cursosList.add(createCurso("6°", "A", "Patricia López", 35, 15, "Inactivo"));

        model.addAttribute("cursosList", cursosList);
        return "clases/index";
    }

    private Map<String, Object> createCurso(String curso, String grupo, String director, int cupos, int estudiantes, String estado) {
        Map<String, Object> c = new HashMap<>();
        c.put("curso", curso);
        c.put("grupo", grupo);
        c.put("nombreCompleto", curso + " - " + grupo);
        c.put("director", director);
        c.put("cuposMaximos", cupos);
        c.put("estudiantes", estudiantes);
        c.put("estado", estado);
        return c;
    }
}
