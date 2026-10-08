package com.siga.siga_iea.asistencias.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siga.siga_iea.asistencias.dto.AsistenciaItemDto;
import com.siga.siga_iea.asistencias.dto.FilaAsistenciaDto;
import com.siga.siga_iea.asistencias.entity.SesionClase;
import com.siga.siga_iea.asistencias.repository.SesionClaseRepository;
import com.siga.siga_iea.asistencias.service.AsistenciaService;
import com.siga.siga_iea.asistencias.service.AsistenciaTablaService;
import com.siga.siga_iea.asistencias.service.MiJornadaService;
import com.siga.siga_iea.auth.service.CurrentUserContextService;
import com.siga.siga_iea.usuarios.entity.Docente;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Controlador de la vista "Mi Jornada" y toma de asistencia docente.
 * No contiene @Transactional para no anular los límites transaccionales de abrirDia.
 * Siempre extrae los datos del operador de CurrentUserContextService, nunca del request.
 */
@Controller
@RequestMapping("/mi-jornada")
public class MiJornadaController {

    private final CurrentUserContextService ctx;
    private final MiJornadaService jornada;
    private final AsistenciaService asistencia;
    private final SesionClaseRepository sesionRepo;
    private final AsistenciaTablaService tabla;
    private final ObjectMapper objectMapper;

    public MiJornadaController(CurrentUserContextService ctx,
                               MiJornadaService jornada,
                               AsistenciaService asistencia,
                               SesionClaseRepository sesionRepo,
                               AsistenciaTablaService tabla,
                               ObjectMapper objectMapper) {
        this.ctx = ctx;
        this.jornada = jornada;
        this.asistencia = asistencia;
        this.sesionRepo = sesionRepo;
        this.tabla = tabla;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("activePage", "mi-jornada");
        Docente d = ctx.getDocenteAutenticado().orElse(null);
        if (d == null) {
            model.addAttribute("sinDocente", true);
            model.addAttribute("esAdminOAdmin", ctx.esAdminOAdministrativo());
            return "mi-jornada/index";
        }
        model.addAttribute("sinDocente", false);
        model.addAttribute("docente", d);
        model.addAttribute("vista", jornada.vistaHoy(d));
        return "mi-jornada/index";
    }

    @PostMapping("/iniciar")
    public String iniciar(Model model) {
        Docente d = ctx.getDocenteAutenticado()
                .orElseThrow(() -> new AccessDeniedException("No se encontró docente autenticado para iniciar jornada"));
        model.addAttribute("vista", jornada.iniciar(d));
        return "mi-jornada/fragments :: sesiones";
    }

    @GetMapping("/sesiones/{id}")
    public String sesion(@PathVariable UUID id, Model model) {
        SesionClase s = sesionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión no encontrada: " + id));
        autorizar(s);

        List<FilaAsistenciaDto> filas = tabla.filas(s);
        model.addAttribute("sesion", s);
        model.addAttribute("filas", filas);

        Map<String, String> mapaEstados = new LinkedHashMap<>();
        for (FilaAsistenciaDto f : filas) {
            if (f.estado() != null) {
                mapaEstados.put(f.id().toString(), f.estado());
            }
        }

        try {
            model.addAttribute("estadosJson", objectMapper.writeValueAsString(mapaEstados));
        } catch (JsonProcessingException e) {
            model.addAttribute("estadosJson", "{}");
        }

        return "mi-jornada/fragments :: tabla";
    }

    @PostMapping("/sesiones/{id}/guardar")
    public String guardar(@PathVariable UUID id,
                          @RequestParam(name = "estudianteId", required = false) List<UUID> estudianteId,
                          @RequestParam(name = "estado", required = false) List<String> estado,
                          Model model) {
        if (estudianteId == null || estado == null || estudianteId.size() != estado.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de estudiantes y estados deben tener la misma dimensión");
        }

        List<AsistenciaItemDto> items = IntStream.range(0, estado.size())
                .mapToObj(i -> new AsistenciaItemDto(estudianteId.get(i), estado.get(i), null))
                .toList();

        try {
            asistencia.registrar(id, items, ctx.getDocenteAutenticado().orElse(null), ctx.getRolAutenticado());
            model.addAttribute("ok", true);
            model.addAttribute("mensaje", "Asistencia registrada exitosamente.");
        } catch (SecurityException e) {
            model.addAttribute("ok", false);
            model.addAttribute("error", "No tienes permiso sobre esta sesión o la ventana de edición está cerrada.");
            return "mi-jornada/fragments :: mensaje";
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("ok", false);
            model.addAttribute("error", e.getMessage());
            return "mi-jornada/fragments :: mensaje";
        }

        return "mi-jornada/fragments :: mensaje";
    }

    private void autorizar(SesionClase s) {
        if (ctx.esAdminOAdministrativo()) {
            return;
        }
        UUID mio = ctx.getDocenteAutenticado().map(Docente::getId).orElse(null);
        if (mio == null || s.getDocente() == null || !mio.equals(s.getDocente().getId())) {
            throw new AccessDeniedException("Sesión ajena");
        }
    }
}
