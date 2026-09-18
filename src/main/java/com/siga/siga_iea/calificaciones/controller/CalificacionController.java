package com.siga.siga_iea.calificaciones.controller;

import com.siga.siga_iea.calificaciones.dto.BoletinMateriaDTO;
import com.siga.siga_iea.calificaciones.service.CalificacionesService;
import com.siga.siga_iea.clases.entity.Clase;
import com.siga.siga_iea.clases.service.ClaseService;
import com.siga.siga_iea.configuracion.service.PeriodoConfigService;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class CalificacionController {

    private final CalificacionesService calificacionesService;
    private final EstudianteService estudianteService;
    private final ClaseService claseService;
    private final PeriodoConfigService periodoConfigService;

    public CalificacionController(CalificacionesService calificacionesService, 
                                  EstudianteService estudianteService,
                                  ClaseService claseService,
                                  PeriodoConfigService periodoConfigService) {
        this.calificacionesService = calificacionesService;
        this.estudianteService = estudianteService;
        this.claseService = claseService;
        this.periodoConfigService = periodoConfigService;
    }

    @GetMapping("/calificaciones")
    public String index(@RequestParam(value = "estudianteId", required = false) UUID estudianteId, Model model) {
        model.addAttribute("title", "Boletín de Calificaciones – IEACI");
        model.addAttribute("activePage", "calificaciones");

        List<Estudiante> estudiantes = estudianteService.listarTodos();
        model.addAttribute("estudiantes", estudiantes);

        Estudiante seleccionado = null;
        if (estudianteId != null) {
            seleccionado = estudianteService.buscarPorId(estudianteId).orElse(null);
        }
        if (seleccionado == null && !estudiantes.isEmpty()) {
            seleccionado = estudiantes.get(0);
        }

        Map<Integer, BigDecimal> ponderaciones = periodoConfigService.getPonderaciones();
        model.addAttribute("ponderaciones", ponderaciones);
        model.addAttribute("pesoP1", periodoConfigService.getPesoPeriodo1());
        model.addAttribute("pesoP2", periodoConfigService.getPesoPeriodo2());
        model.addAttribute("pesoP3", periodoConfigService.getPesoPeriodo3());

        if (seleccionado != null) {
            model.addAttribute("estudianteSeleccionadoId", seleccionado.getId().toString());
            model.addAttribute("estudianteNombre", seleccionado.getNombreCompleto());
            model.addAttribute("estudianteDocumento", seleccionado.getNumeroDocumento());
            model.addAttribute("estudianteGrado", "11° - 01");

            List<BoletinMateriaDTO> boletin = calificacionesService.obtenerBoletinEstudiante(seleccionado.getId(), "2026", ponderaciones);
            model.addAttribute("boletin", boletin);
        } else {
            model.addAttribute("estudianteSeleccionadoId", "");
            model.addAttribute("estudianteNombre", "Sin estudiante registrado");
            model.addAttribute("estudianteDocumento", "-");
            model.addAttribute("estudianteGrado", "-");
            model.addAttribute("boletin", Collections.emptyList());
        }

        List<Clase> cursos = claseService.listarTodosLosCursos();
        model.addAttribute("cursosList", cursos);

        return "calificaciones/index";
    }
}
