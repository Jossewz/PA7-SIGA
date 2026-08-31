package com.siga.siga_iea.reportes.controller;

import com.siga.siga_iea.reportes.entity.Reporte;
import com.siga.siga_iea.reportes.service.ReportesService;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.PersonalAdministrativo;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import com.siga.siga_iea.usuarios.service.PersonalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UsuarioRepository usuarioRepository;

    public ReporteController(ReportesService reportesService,
                             EstudianteService estudianteService,
                             PersonalService personalService,
                             UsuarioRepository usuarioRepository) {
        this.reportesService = reportesService;
        this.estudianteService = estudianteService;
        this.personalService = personalService;
        this.usuarioRepository = usuarioRepository;
    }

    private Optional<Usuario> getUsuarioLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return usuarioRepository.findByEmail(auth.getName());
        }
        return Optional.empty();
    }

    private Optional<Estudiante> getEstudianteLogueado() {
        return getUsuarioLogueado()
                .map(Usuario::getNumeroDocumento)
                .filter(doc -> doc != null && !doc.isBlank())
                .flatMap(estudianteService::buscarPorNumeroDocumento);
    }

    private Optional<Docente> getDocenteLogueado() {
        return getUsuarioLogueado()
                .map(Usuario::getNumeroDocumento)
                .filter(doc -> doc != null && !doc.isBlank())
                .flatMap(doc -> personalService.buscarDocentes(doc, null).stream().findFirst());
    }

    private Optional<PersonalAdministrativo> getPersonalLogueado() {
        return getUsuarioLogueado()
                .map(Usuario::getNumeroDocumento)
                .filter(doc -> doc != null && !doc.isBlank())
                .flatMap(doc -> personalService.buscarPersonal(doc, null, null, null).stream().findFirst());
    }

    @GetMapping("/reportes")
    public String index(Model model) {
        model.addAttribute("title", "Reportes de Estudiantes – IEACI");
        model.addAttribute("activePage", "reportes");

        Optional<Usuario> userOpt = getUsuarioLogueado();
        String userRole = userOpt.map(u -> com.siga.siga_iea.auth.security.CustomUserDetailsService.normalizeRole(u.getRol())).orElse("ESTUDIANTE");

        boolean esAdmin = "ADMIN".equals(userRole);
        boolean esPersonal = "PERSONAL_ADMINISTRATIVO".equals(userRole);
        boolean esDocente = "DOCENTE".equals(userRole);
        Optional<Estudiante> estLogueadoOpt = getEstudianteLogueado();
        boolean esEstudiante = "ESTUDIANTE".equals(userRole) || estLogueadoOpt.isPresent();

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esPersonal", esPersonal);
        model.addAttribute("esDocente", esDocente);
        model.addAttribute("esEstudiante", esEstudiante);
        model.addAttribute("userRole", userRole);

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
        List<Reporte> reportesDB;
        if (esEstudiante && estLogueadoOpt.isPresent()) {
            reportesDB = reportesService.listarPorEstudiante(estLogueadoOpt.get().getId());
        } else {
            reportesDB = reportesService.listarTodos();
        }

        List<Map<String, Object>> reportesMock = new ArrayList<>();

        for (Reporte r : reportesDB) {
            Map<String, Object> map = new HashMap<>();
            map.put("dbId", r.getId().toString());
            map.put("id", r.getCodigo());
            map.put("estudiante", r.getEstudiante() != null ? r.getEstudiante().getNombreCompleto() : "N/A");
            map.put("grado", "11° - 01");
            map.put("docente", r.getDocente() != null ? r.getDocente().getNombreCompleto() : "N/A");
            map.put("categoria", r.getCategoria());
            map.put("razon", r.getRazon());
            map.put("descripcionRazon", r.getDescripcionRazon() != null ? r.getDescripcionRazon() : "");
            map.put("detalles", r.getDetalles());
            map.put("fecha", r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate().toString() : "");
            map.put("estado", r.getEstado());
            map.put("fechaCitacion", r.getFechaCitacion() != null ? r.getFechaCitacion().toString().replace("T", " ") : null);
            map.put("requiereAcudiente", Boolean.TRUE.equals(r.getRequiereAcudiente()));
            map.put("observacionesAdmin", r.getObservacionesAdmin() != null ? r.getObservacionesAdmin() : "Sin observaciones adicionales de Dirección.");
            reportesMock.add(map);
        }

        model.addAttribute("estudiantesList", estudiantesList);
        model.addAttribute("reportesMock", reportesMock);
        return "reportes/index";
    }

    @PostMapping("/reportes/crear")
    public String crearReporte(
            @RequestParam("estudianteId") UUID estudianteId,
            @RequestParam(value = "docenteId", required = false) UUID docenteId,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam("razon") String razon,
            @RequestParam(value = "descripcionRazon", required = false) String descripcionRazon,
            @RequestParam("detalles") String detalles,
            RedirectAttributes redirectAttributes) {

        try {
            UUID targetDocenteId = docenteId;
            if (targetDocenteId == null) {
                Optional<Docente> docOpt = getDocenteLogueado();
                if (docOpt.isPresent()) {
                    targetDocenteId = docOpt.get().getId();
                } else {
                    List<Docente> docentes = personalService.listarDocentes();
                    if (!docentes.isEmpty()) {
                        targetDocenteId = docentes.get(0).getId();
                    } else {
                        Docente d = new Docente();
                        d.setNombres("Jorge Eliécer");
                        d.setApellidos("Rojas");
                        d.setTipoDocumento("CC");
                        d.setNumeroDocumento("1088123456");
                        d.setEstado("Activo");
                        d = personalService.guardarDocente(d);
                        targetDocenteId = d.getId();
                    }
                }
            }

            String targetCategoria = categoria;
            if (targetCategoria == null || targetCategoria.isBlank()) {
                if (Arrays.asList("Agresión", "Falta de respeto", "Daño a la escuela").contains(razon)) {
                    targetCategoria = "Convivencia y Disciplina";
                } else if (Arrays.asList("Ausencias", "Llegadas tarde", "Salud o riesgo").contains(razon)) {
                    targetCategoria = "Asistencia y Salud";
                } else if ("Otro".equals(razon)) {
                    targetCategoria = "Otras Razones";
                } else {
                    targetCategoria = "Razones Académicas";
                }
            }

            reportesService.crearReporte(estudianteId, targetDocenteId, targetCategoria, razon, descripcionRazon, detalles);
            redirectAttributes.addFlashAttribute("mensajeExito", "Reporte registrado exitosamente en el sistema.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar reporte: " + ex.getMessage());
        }

        return "redirect:/reportes";
    }

    @PostMapping("/reportes/atender")
    public String atenderReporte(
            @RequestParam("reporteId") UUID reporteId,
            @RequestParam("decision") String decision,
            @RequestParam(value = "fechaCitacionStr", required = false) String fechaCitacionStr,
            @RequestParam(value = "fechaCitacion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCitacion,
            @RequestParam(value = "requiereAcudiente", required = false, defaultValue = "false") Boolean requiereAcudiente,
            @RequestParam(value = "observacionesAdmin", required = false) String observacionesAdmin,
            @RequestParam(value = "adminId", required = false) UUID adminId,
            RedirectAttributes redirectAttributes) {

        try {
            LocalDateTime parsedDate = fechaCitacion;
            if (parsedDate == null && fechaCitacionStr != null && !fechaCitacionStr.isBlank()) {
                try {
                    parsedDate = LocalDateTime.parse(fechaCitacionStr.replace(" ", "T"));
                } catch (Exception ignored) {
                    try {
                        String cleaned = fechaCitacionStr.split(" - ")[0] + "T" + (fechaCitacionStr.contains(" - ") ? fechaCitacionStr.split(" - ")[1].replace(" AM", "").replace(" PM", "") : "08:00");
                        parsedDate = LocalDateTime.parse(cleaned);
                    } catch (Exception ignored2) {
                        parsedDate = LocalDateTime.now().plusDays(2);
                    }
                }
            }

            UUID targetAdminId = adminId;
            if (targetAdminId == null) {
                Optional<PersonalAdministrativo> adminOpt = getPersonalLogueado();
                if (adminOpt.isPresent()) {
                    targetAdminId = adminOpt.get().getId();
                }
            }

            reportesService.atenderReporte(reporteId, decision, parsedDate, requiereAcudiente, observacionesAdmin, targetAdminId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Reporte procesado y citación emitida exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar reporte: " + ex.getMessage());
        }

        return "redirect:/reportes";
    }
}
