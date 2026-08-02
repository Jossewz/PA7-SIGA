package com.siga.siga_iea.reportes.controller;

import com.siga.siga_iea.reportes.entity.Reporte;
import com.siga.siga_iea.reportes.service.ReportesService;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import com.siga.siga_iea.usuarios.service.PersonalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class ReporteController {

    private final ReportesService reportesService;
    private final EstudianteService estudianteService;
    private final PersonalService personalService;

    public ReporteController(ReportesService reportesService,
                             EstudianteService estudianteService,
                             PersonalService personalService) {
        this.reportesService = reportesService;
        this.estudianteService = estudianteService;
        this.personalService = personalService;
    }

    @GetMapping("/reportes")
    public String index(Model model) {
        model.addAttribute("title", "Reportes de Estudiantes – IEACI");
        model.addAttribute("activePage", "reportes");

        // Fetch students for report dropdown
        List<Estudiante> estudiantesDB = estudianteService.listarTodos();
        List<Map<String, Object>> estudiantesList = new ArrayList<>();

        for (Estudiante e : estudiantesDB) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId().toString());
            map.put("nombre", e.getNombreCompleto());
            map.put("grado", "11° - 01");
            estudiantesList.add(map);
        }

        if (estudiantesList.isEmpty()) {
            estudiantesList.add(createEstudianteMock("1", "Álvarez Restrepo, Mateo", "11° - 01"));
            estudiantesList.add(createEstudianteMock("2", "Bermúdez Castro, Sofia", "11° - 01"));
            estudiantesList.add(createEstudianteMock("3", "Cárdenas Morales, Juan Diego", "11° - 01"));
            estudiantesList.add(createEstudianteMock("4", "Díaz Gómez, Valentina", "11° - 01"));
            estudiantesList.add(createEstudianteMock("5", "Espinosa Vargas, Andrés Felipe", "11° - 01"));
            estudiantesList.add(createEstudianteMock("6", "Franco Gutiérrez, Isabella", "11° - 01"));
            estudiantesList.add(createEstudianteMock("7", "Gómez Hernández, Santiago", "11° - 01"));
        }

        // Fetch reports
        List<Reporte> reportesDB = reportesService.listarTodos();
        List<Map<String, Object>> reportesMock = new ArrayList<>();

        for (Reporte r : reportesDB) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getCodigo());
            map.put("estudiante", r.getEstudiante() != null ? r.getEstudiante().getNombreCompleto() : "N/A");
            map.put("grado", "11° - 01");
            map.put("docente", r.getDocente() != null ? r.getDocente().getNombreCompleto() : "N/A");
            map.put("categoria", r.getCategoria());
            map.put("razon", r.getRazon());
            map.put("descripcionRazon", r.getDescripcionRazon());
            map.put("detalles", r.getDetalles());
            map.put("fecha", r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate().toString() : "");
            map.put("estado", r.getEstado());
            map.put("fechaCitacion", r.getFechaCitacion() != null ? r.getFechaCitacion().toString() : null);
            map.put("requiereAcudiente", r.getRequiereAcudiente());
            map.put("observacionesAdmin", r.getObservacionesAdmin());
            reportesMock.add(map);
        }

        if (reportesMock.isEmpty()) {
            reportesMock.add(createReporte(
                "REP-2026-001", "Mateo Álvarez Restrepo", "11° - 01", "Prof. Jorge Eliécer Rojas",
                "Razones Académicas", "Bajo rendimiento", "Notas muy bajas o pérdida constante de materias.",
                "El estudiante presentó 1.8 en la última evaluación de matemáticas.", "2026-07-29",
                "Aceptado", "2026-08-04 - 08:30 AM", true, "Se requiere presencia del acudiente."
            ));
            reportesMock.add(createReporte(
                "REP-2026-002", "Juan Diego Cárdenas", "11° - 01", "Prof. Ana María Sánchez",
                "Razones de Convivencia y Disciplina", "Falta de respeto", "Desobedecer reglas.",
                "Incumplimiento reiterado de las normas de convivencia.", "2026-07-30",
                "Aceptado", "2026-08-05 - 10:00 AM", false, "Citación individual."
            ));
            reportesMock.add(createReporte(
                "REP-2026-003", "Santiago Gómez Hernández", "11° - 01", "Prof. Carlos Mendoza",
                "Razones de Asistencia y Salud", "Ausencias", "Faltar mucho sin permiso.",
                "Registra 4 inasistencias consecutivas no justificadas.", "2026-07-31",
                "Pendiente", null, false, null
            ));
        }

        model.addAttribute("estudiantesList", estudiantesList);
        model.addAttribute("reportesMock", reportesMock);
        return "reportes/index";
    }

    @PostMapping("/reportes/crear")
    public String crearReporte(
            @RequestParam("estudianteId") UUID estudianteId,
            @RequestParam("docenteId") UUID docenteId,
            @RequestParam("categoria") String categoria,
            @RequestParam("razon") String razon,
            @RequestParam(value = "descripcionRazon", required = false) String descripcionRazon,
            @RequestParam("detalles") String detalles,
            RedirectAttributes redirectAttributes) {

        try {
            reportesService.crearReporte(estudianteId, docenteId, categoria, razon, descripcionRazon, detalles);
            redirectAttributes.addFlashAttribute("mensajeExito", "Reporte registrado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar reporte: " + ex.getMessage());
        }

        return "redirect:/reportes";
    }

    @PostMapping("/reportes/atender")
    public String atenderReporte(
            @RequestParam("reporteId") UUID reporteId,
            @RequestParam("decision") String decision,
            @RequestParam(value = "fechaCitacion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCitacion,
            @RequestParam(value = "requiereAcudiente", required = false, defaultValue = "false") Boolean requiereAcudiente,
            @RequestParam(value = "observacionesAdmin", required = false) String observacionesAdmin,
            @RequestParam(value = "adminId", required = false) UUID adminId,
            RedirectAttributes redirectAttributes) {

        try {
            reportesService.atenderReporte(reporteId, decision, fechaCitacion, requiereAcudiente, observacionesAdmin, adminId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Reporte procesado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar reporte: " + ex.getMessage());
        }

        return "redirect:/reportes";
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
