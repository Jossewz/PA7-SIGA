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
}
