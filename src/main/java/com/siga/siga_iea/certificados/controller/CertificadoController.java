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

        // Student Logged In Info
        model.addAttribute("estudianteNombre", "Mateo Álvarez Restrepo");
        model.addAttribute("estudianteGrado", "11° - 01");
        model.addAttribute("estudianteDocumento", "1098432101");

        List<SolicitudCertificado> solicitudesDB = certificadoService.listarTodas();
        List<Map<String, Object>> solicitudesMock = new ArrayList<>();

        for (SolicitudCertificado s : solicitudesDB) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getCodigo());
            map.put("estudiante", s.getEstudiante() != null ? s.getEstudiante().getNombreCompleto() : "Mateo Álvarez Restrepo");
            map.put("documento", s.getEstudiante() != null ? s.getEstudiante().getNumeroDocumento() : "1098432101");
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

        if (solicitudesMock.isEmpty()) {
            solicitudesMock.add(createSolicitud(
                "CERT-2026-001", "Mateo Álvarez Restrepo", "1098432101", "11° - 01",
                "Constancia de matrícula", "Constancias Administrativas",
                "Trámite de subsidio familiar en Caja de Compensación", "2026-07-28", "Resuelto",
                "Se expide la constancia de matrícula activa para el año lectivo 2026.",
                "Constancia_Matricula_MateoAlvarez.pdf", "Coordinación Académica"
            ));

            solicitudesMock.add(createSolicitud(
                "CERT-2026-002", "Sofia Bermúdez Castro", "1098432102", "11° - 01",
                "Certificado de notas", "Certificados Académicos",
                "Ingreso a procesos de selección universitaria", "2026-07-30", "Resuelto",
                "Adjunto certificado con el historial de notas por asignaturas.",
                "Certificado_Notas_SofiaBermudez.pdf", "Secretaría Académica"
            ));

            solicitudesMock.add(createSolicitud(
                "CERT-2026-003", "Mateo Álvarez Restrepo", "1098432101", "11° - 01",
                "Paz y salvo", "Constancias Administrativas",
                "Verificación de estado de pago de pensión", "2026-07-31", "Pendiente",
                null, null, null
            ));
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
                    Estudiante est = new Estudiante();
                    est.setNombres("Mateo");
                    est.setApellidos("Álvarez Restrepo");
                    est.setNumeroDocumento("1098432101");
                    est.setTipoDocumento("CC");
                    est = estudianteService.guardar(est);
                    estudianteId = est.getId();
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

            // If responding to a mock item not yet in DB, create it first
            SolicitudCertificado sol;
            if (solOpt.isPresent()) {
                sol = solOpt.get();
            } else {
                List<Estudiante> todos = estudianteService.listarTodos();
                Estudiante est;
                if (!todos.isEmpty()) {
                    est = todos.get(0);
                } else {
                    est = new Estudiante();
                    est.setNombres("Mateo");
                    est.setApellidos("Álvarez Restrepo");
                    est.setNumeroDocumento("1098432101");
                    est = estudianteService.guardar(est);
                }
                sol = certificadoService.crearSolicitud(est.getId(), "Certificado de estudios", "Constancias Administrativas", "Solicitud procesada", "2026", "11°");
                sol.setCodigo(solicitudIdStr);
            }

            certificadoService.responderSolicitud(sol.getId(), mensajeRespuesta, archivoPDF, adminId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud " + sol.getCodigo() + " resuelta y documento adjuntado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al responder solicitud: " + ex.getMessage());
        }

        return "redirect:/certificados";
    }

    private Map<String, Object> createSolicitud(String id, String estudiante, String doc, String grado, String tipo, String categoria, String motivo, String fecha, String estado, String mensajeRespuesta, String archivoAdjunto, String respondidoPor) {
        Map<String, Object> s = new HashMap<>();
        s.put("id", id);
        s.put("estudiante", estudiante);
        s.put("documento", doc);
        s.put("grado", grado);
        s.put("tipo", tipo);
        s.put("categoria", categoria);
        s.put("motivo", motivo);
        s.put("fecha", fecha);
        s.put("estado", estado);
        s.put("mensajeRespuesta", mensajeRespuesta);
        s.put("archivoAdjunto", archivoAdjunto);
        s.put("respondidoPor", respondidoPor);
        return s;
    }
}
