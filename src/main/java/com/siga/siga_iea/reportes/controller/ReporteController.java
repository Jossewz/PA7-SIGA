package com.siga.siga_iea.reportes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReporteController {

    @GetMapping("/reportes")
    public String index(Model model) {
        model.addAttribute("title", "Reportes de Estudiantes – IEACI");
        model.addAttribute("activePage", "reportes");

        // Mock list of students for reports
        List<Map<String, Object>> estudiantesList = new ArrayList<>();
        estudiantesList.add(createEstudianteMock("1", "Álvarez Restrepo, Mateo", "11° - 01"));
        estudiantesList.add(createEstudianteMock("2", "Bermúdez Castro, Sofia", "11° - 01"));
        estudiantesList.add(createEstudianteMock("3", "Cárdenas Morales, Juan Diego", "11° - 01"));
        estudiantesList.add(createEstudianteMock("4", "Díaz Gómez, Valentina", "11° - 01"));
        estudiantesList.add(createEstudianteMock("5", "Espinosa Vargas, Andrés Felipe", "11° - 01"));
        estudiantesList.add(createEstudianteMock("6", "Franco Gutiérrez, Isabella", "11° - 01"));
        estudiantesList.add(createEstudianteMock("7", "Gómez Hernández, Santiago", "11° - 01"));

        // Mock list of reports created by teachers
        List<Map<String, Object>> reportesMock = new ArrayList<>();

        reportesMock.add(createReporte(
            "REP-2026-001",
            "Mateo Álvarez Restrepo",
            "11° - 01",
            "Prof. Jorge Eliécer Rojas",
            "Razones Académicas",
            "Bajo rendimiento",
            "Notas muy bajas o pérdida constante de materias.",
            "El estudiante presentó 1.8 en la última evaluación de matemáticas y acumula 3 faltas de entrega de talleres.",
            "2026-07-29",
            "Aceptado",
            "2026-08-04 - 08:30 AM",
            true,
            "Se requiere presencia del acudiente por bajo desempeño acumulado en el 2° periodo."
        ));

        reportesMock.add(createReporte(
            "REP-2026-002",
            "Juan Diego Cárdenas",
            "11° - 01",
            "Prof. Ana María Sánchez",
            "Razones de Convivencia y Disciplina",
            "Falta de respeto",
            "Desobedecer reglas o contestar mal a la autoridad.",
            "Incumplimiento reiterado de las normas de convivencia durante la clase de Lengua Castellana.",
            "2026-07-30",
            "Aceptado",
            "2026-08-05 - 10:00 AM",
            false,
            "Citación individual con Coordinación de Convivencia."
        ));

        reportesMock.add(createReporte(
            "REP-2026-003",
            "Santiago Gómez Hernández",
            "11° - 01",
            "Prof. Carlos Mendoza",
            "Razones de Asistencia y Salud",
            "Ausencias",
            "Faltar mucho sin permiso o aviso de los padres.",
            "Registra 4 inasistencias consecutivas no justificadas esta semana.",
            "2026-07-31",
            "Pendiente",
            null,
            false,
            null
        ));

        model.addAttribute("estudiantesList", estudiantesList);
        model.addAttribute("reportesMock", reportesMock);
        return "reportes/index";
    }

    private Map<String, Object> createEstudianteMock(String id, String nombre, String grado) {
        Map<String, Object> e = new HashMap<>();
        e.put("id", id);
        e.put("nombre", nombre);
        e.put("grado", grado);
        return e;
    }

    private Map<String, Object> createReporte(String id, String estudiante, String grado, String docente, String categoria, String razon, String descripcionRazon, String detalles, String fecha, String estado, String fechaCitacion, boolean requiereAcudiente, String observacionesAdmin) {
        Map<String, Object> r = new HashMap<>();
        r.put("id", id);
        r.put("estudiante", estudiante);
        r.put("grado", grado);
        r.put("docente", docente);
        r.put("categoria", categoria);
        r.put("razon", razon);
        r.put("descripcionRazon", descripcionRazon);
        r.put("detalles", detalles);
        r.put("fecha", fecha);
        r.put("estado", estado);
        r.put("fechaCitacion", fechaCitacion);
        r.put("requiereAcudiente", requiereAcudiente);
        r.put("observacionesAdmin", observacionesAdmin);
        return r;
    }
}
