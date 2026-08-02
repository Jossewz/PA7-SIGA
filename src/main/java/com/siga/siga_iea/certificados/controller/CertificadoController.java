package com.siga.siga_iea.certificados.controller;

import com.siga.siga_iea.certificados.entity.SolicitudCertificado;
import com.siga.siga_iea.certificados.service.CertificadoService;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class CertificadoController {

    private final CertificadoService certificadoService;
    private final EstudianteService estudianteService;

    public CertificadoController(CertificadoService certificadoService, EstudianteService estudianteService) {
        this.certificadoService = certificadoService;
        this.estudianteService = estudianteService;
    }

    @GetMapping("/certificados")
    public String index(Model model) {
        model.addAttribute("title", "Certificados y Constancias – IEACI");
        model.addAttribute("activePage", "certificados");

        List<Estudiante> todosEstudiantes = estudianteService.listarTodos();
        if (!todosEstudiantes.isEmpty()) {
            Estudiante primerEstudiante = todosEstudiantes.get(0);
            model.addAttribute("estudianteNombre", primerEstudiante.getNombreCompleto());
            model.addAttribute("estudianteGrado", "11° - 01");
            model.addAttribute("estudianteDocumento", primerEstudiante.getNumeroDocumento());
            model.addAttribute("estudianteId", primerEstudiante.getId());
        } else {
            model.addAttribute("estudianteNombre", "Sin estudiantes registrados");
            model.addAttribute("estudianteGrado", "-");
            model.addAttribute("estudianteDocumento", "-");
            model.addAttribute("estudianteId", null);
        }

        List<SolicitudCertificado> solicitudesDB = certificadoService.listarTodas();
        List<Map<String, Object>> solicitudesMock = new ArrayList<>();

        for (SolicitudCertificado s : solicitudesDB) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getCodigo());
            map.put("estudiante", s.getEstudiante() != null ? s.getEstudiante().getNombreCompleto() : "N/A");
            map.put("documento", s.getEstudiante() != null ? s.getEstudiante().getNumeroDocumento() : "N/A");
            map.put("grado", s.getGradoReferencia() != null ? s.getGradoReferencia() : "11° - 01");
            map.put("tipo", s.getTipo());
            map.put("categoria", s.getCategoria());
            map.put("motivo", s.getMotivo());
            map.put("fecha", s.getCreatedAt() != null ? s.getCreatedAt().toLocalDate().toString() : "");
            map.put("estado", s.getEstado());
            map.put("mensajeRespuesta", s.getMensajeRespuesta());
            map.put("archivoAdjunto", s.getArchivoAdjuntoKey() != null ? "/storage/public/view?key=" + s.getArchivoAdjuntoKey() : null);
            map.put("respondidoPor", s.getRespondidoPor() != null ? s.getRespondidoPor().getNombreCompleto() : "Secretaría Académica");
            solicitudesMock.add(map);
        }

        model.addAttribute("solicitudesMock", solicitudesMock);
        return "certificados/index";
    }

    @PostMapping("/certificados/solicitar")
    public String solicitarCertificado(
            @RequestParam(value = "estudianteId", required = false) UUID estudianteId,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam("motivo") String motivo,
            @RequestParam(value = "anoLectivo", required = false, defaultValue = "2026") String anoLectivo,
            @RequestParam(value = "gradoReferencia", required = false, defaultValue = "11°") String gradoReferencia,
            RedirectAttributes redirectAttributes) {

        try {
            if (estudianteId == null) {
                List<Estudiante> todos = estudianteService.listarTodos();
                if (!todos.isEmpty()) {
                    estudianteId = todos.get(0).getId();
                } else {
                    throw new IllegalArgumentException("No hay estudiantes registrados para realizar la solicitud.");
                }
            }

            SolicitudCertificado sol = certificadoService.crearSolicitud(estudianteId, tipo, categoria, motivo, anoLectivo, gradoReferencia);
            redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud (" + sol.getCodigo() + ") de " + tipo + " enviada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al solicitar certificado: " + ex.getMessage());
        }

        return "redirect:/certificados";
    }

    @PostMapping("/certificados/responder")
    public String responderSolicitud(
            @RequestParam("solicitudId") String solicitudIdStr,
            @RequestParam("mensajeRespuesta") String mensajeRespuesta,
            @RequestParam(value = "archivoPDF", required = false) MultipartFile archivoPDF,
            @RequestParam(value = "adminId", required = false) UUID adminId,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<SolicitudCertificado> solOpt = Optional.empty();

            try {
                UUID uuid = UUID.fromString(solicitudIdStr);
                solOpt = certificadoService.buscarPorId(uuid);
            } catch (IllegalArgumentException ignored) {}

            if (solOpt.isEmpty()) {
                solOpt = certificadoService.buscarPorCodigo(solicitudIdStr);
            }

            if (solOpt.isEmpty()) {
                throw new IllegalArgumentException("No se encontró la solicitud de certificado especificada.");
            }

            SolicitudCertificado sol = solOpt.get();
            certificadoService.responderSolicitud(sol.getId(), mensajeRespuesta, archivoPDF, adminId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud " + sol.getCodigo() + " resuelta y documento adjuntado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al responder solicitud: " + ex.getMessage());
        }

        return "redirect:/certificados";
    }
}
