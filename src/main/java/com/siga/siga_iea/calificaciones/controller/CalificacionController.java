package com.siga.siga_iea.calificaciones.controller;

import com.siga.siga_iea.calificaciones.service.CalificacionesService;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CalificacionController {

    private final CalificacionesService calificacionesService;
    private final EstudianteService estudianteService;

    public CalificacionController(CalificacionesService calificacionesService, EstudianteService estudianteService) {
        this.calificacionesService = calificacionesService;
        this.estudianteService = estudianteService;
    }

    @GetMapping("/calificaciones")
    public String index(Model model) {
        model.addAttribute("title", "Boletín de Calificaciones – IEACI");
        model.addAttribute("activePage", "calificaciones");

        List<Estudiante> estudiantes = estudianteService.listarTodos();
        if (!estudiantes.isEmpty()) {
            Estudiante e = estudiantes.get(0);
            model.addAttribute("estudianteNombre", e.getNombreCompleto());
            model.addAttribute("estudianteGrado", "11° - 01");
            model.addAttribute("estudianteDocumento", e.getNumeroDocumento());
        } else {
            model.addAttribute("estudianteNombre", "Sin estudiante registrado");
            model.addAttribute("estudianteGrado", "-");
            model.addAttribute("estudianteDocumento", "-");
        }

        List<Map<String, Object>> asignaturas = new ArrayList<>();
        model.addAttribute("asignaturas", asignaturas);
        return "calificaciones/index";
    }
}
