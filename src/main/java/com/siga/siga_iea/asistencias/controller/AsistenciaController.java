package com.siga.siga_iea.asistencias.controller;

import com.siga.siga_iea.asistencias.dto.AsistenciaItemDto;
import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.entity.SesionClase;
import com.siga.siga_iea.asistencias.service.AsistenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/asistencias")
    public String index(Model model) {
        model.addAttribute("title", "Asistencias – IEACI");
        model.addAttribute("activePage", "asistencias");
        return "asistencias/index";
    }

    /**
     * Endpoint HTTP para apertura de día de un curso.
     * Idempotente y resiliente ante concurrencia.
     */
    @PostMapping("/asistencias/abrir-dia")
    @ResponseBody
    public ResponseEntity<List<SesionClase>> abrirDia(
            @RequestParam("cursoId") UUID cursoId,
            @RequestParam(value = "fecha", required = false) String fechaStr) {
        LocalDate fecha = (fechaStr != null && !fechaStr.isBlank()) ? LocalDate.parse(fechaStr) : LocalDate.now();
        List<SesionClase> sesiones = asistenciaService.abrirDia(cursoId, fecha);
        return ResponseEntity.ok(sesiones);
    }

    /**
     * Endpoint HTTP de alta frecuencia para registro masivo de asistencia (hasta 40 estudiantes por sesión).
     * Realiza upsert y valida pertenencia y autorización.
     */
    @PostMapping("/asistencias/sesiones/{sesionId}/registrar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarAsistencia(
            @PathVariable("sesionId") UUID sesionId,
            @RequestBody List<AsistenciaItemDto> items) {
        List<Asistencia> registradas = asistenciaService.registrar(sesionId, items);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "sesionId", sesionId,
                "registrosProcesados", registradas.size()
        ));
    }

    /**
     * Endpoint de consulta para lectura de asistencia de un curso en una fecha y materia.
     */
    @GetMapping("/asistencias/curso/{cursoId}/mapa")
    @ResponseBody
    public ResponseEntity<Map<UUID, String>> obtenerMapa(
            @PathVariable("cursoId") UUID cursoId,
            @RequestParam(value = "fecha", required = false) String fechaStr,
            @RequestParam(value = "materiaId", required = false) UUID materiaId) {
        LocalDate fecha = (fechaStr != null && !fechaStr.isBlank()) ? LocalDate.parse(fechaStr) : LocalDate.now();
        Map<UUID, String> mapa = asistenciaService.obtenerMapaEstadosAsistencia(cursoId, fecha, materiaId);
        return ResponseEntity.ok(mapa);
    }
}
