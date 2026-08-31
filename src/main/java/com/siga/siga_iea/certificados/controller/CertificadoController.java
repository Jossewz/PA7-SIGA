package com.siga.siga_iea.certificados.controller;

import com.siga.siga_iea.certificados.entity.SolicitudCertificado;
import com.siga.siga_iea.certificados.service.CertificadoService;
import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UsuarioRepository usuarioRepository;
    private final MatriculaRepository matriculaRepository;

    public CertificadoController(CertificadoService certificadoService,
                                 EstudianteService estudianteService,
                                 UsuarioRepository usuarioRepository,
                                 MatriculaRepository matriculaRepository) {
        this.certificadoService = certificadoService;
        this.estudianteService = estudianteService;
        this.usuarioRepository = usuarioRepository;
        this.matriculaRepository = matriculaRepository;
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

    @GetMapping("/certificados")
    public String index(Model model) {
        model.addAttribute("title", "Certificados y Constancias – IEACI");
        model.addAttribute("activePage", "certificados");

        Optional<Usuario> userOpt = getUsuarioLogueado();
        String userRole = userOpt.map(u -> com.siga.siga_iea.auth.security.CustomUserDetailsService.normalizeRole(u.getRol())).orElse("ESTUDIANTE");

        boolean esAdmin = "ADMIN".equals(userRole);
        boolean esPersonal = "PERSONAL_ADMINISTRATIVO".equals(userRole);
        Optional<Estudiante> estLogueadoOpt = getEstudianteLogueado();
        boolean esEstudiante = "ESTUDIANTE".equals(userRole) || estLogueadoOpt.isPresent();

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esPersonal", esPersonal);
        model.addAttribute("esEstudiante", esEstudiante);
        model.addAttribute("userRole", userRole);
        model.addAttribute("listaEstudiantes", estudianteService.listarTodos());

        Estudiante estudianteSeleccionado = null;
        List<SolicitudCertificado> solicitudesEstudianteDB;

        if (esEstudiante) {
            estudianteSeleccionado = estLogueadoOpt.get();
            solicitudesEstudianteDB = certificadoService.listarPorEstudiante(estudianteSeleccionado.getId());
        } else {
            List<Estudiante> todosEstudiantes = estudianteService.listarTodos();
            if (!todosEstudiantes.isEmpty()) {
                estudianteSeleccionado = todosEstudiantes.get(0);
                solicitudesEstudianteDB = certificadoService.listarPorEstudiante(estudianteSeleccionado.getId());
            } else {
                solicitudesEstudianteDB = Collections.emptyList();
            }
        }

        if (estudianteSeleccionado != null) {
            model.addAttribute("estudianteNombre", estudianteSeleccionado.getNombreCompleto());
            Optional<Matricula> matOpt = matriculaRepository.findTopByEstudianteIdOrderByFechaMatriculaDesc(estudianteSeleccionado.getId());
            String grado = matOpt.map(m -> m.getGrado() != null ? m.getGrado() : "11°").orElse("11°");
            model.addAttribute("estudianteGrado", grado);
            model.addAttribute("estudianteDocumento", estudianteSeleccionado.getNumeroDocumento());
            model.addAttribute("estudianteId", estudianteSeleccionado.getId());
        } else {
            model.addAttribute("estudianteNombre", "Sin estudiantes registrados");
            model.addAttribute("estudianteGrado", "-");
            model.addAttribute("estudianteDocumento", "-");
            model.addAttribute("estudianteId", null);
        }

        // Listar solicitudes del estudiante (privadas)
        List<Map<String, Object>> solicitudesEstudianteMock = mapearSolicitudes(solicitudesEstudianteDB);
        model.addAttribute("solicitudesEstudianteMock", solicitudesEstudianteMock);

        // Listar todas las solicitudes para el rol administrativo
        List<SolicitudCertificado> solicitudesTodasDB = certificadoService.listarTodas();
        List<Map<String, Object>> solicitudesAdminMock = mapearSolicitudes(solicitudesTodasDB);
        model.addAttribute("solicitudesAdminMock", solicitudesAdminMock);

        // Compatibilidad
        model.addAttribute("solicitudesMock", solicitudesEstudianteMock);

        return "certificados/index";
    }

    private List<Map<String, Object>> mapearSolicitudes(List<SolicitudCertificado> lista) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SolicitudCertificado s : lista) {
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
            result.add(map);
        }
        return result;
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
                Optional<Estudiante> estOpt = getEstudianteLogueado();
                if (estOpt.isPresent()) {
                    estudianteId = estOpt.get().getId();
                } else {
                    throw new IllegalArgumentException("Debe seleccionar un estudiante para generar la solicitud.");
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
