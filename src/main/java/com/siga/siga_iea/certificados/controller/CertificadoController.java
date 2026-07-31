package com.siga.siga_iea.certificados.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CertificadoController {

    @GetMapping("/certificados")
    public String index(Model model) {
        model.addAttribute("title", "Certificados y Constancias – IEACI");
        model.addAttribute("activePage", "certificados");

        // Student Logged In Mock Info
        model.addAttribute("estudianteNombre", "Mateo Álvarez Restrepo");
        model.addAttribute("estudianteGrado", "11° - 01");
        model.addAttribute("estudianteDocumento", "1098432101");

        // Mock list of certificate requests
        List<Map<String, Object>> solicitudesMock = new ArrayList<>();
        
        solicitudesMock.add(createSolicitud(
            "CERT-2026-001",
            "Mateo Álvarez Restrepo",
            "1098432101",
            "11° - 01",
            "Constancia de matrícula",
            "Constancias Administrativas",
            "Trámite de subsidio familiar en Caja de Compensación",
            "2026-07-28",
            "Resuelto",
            "Se expide la constancia de matrícula activa para el año lectivo 2026 firmada por Rectoría.",
            "Constancia_Matricula_MateoAlvarez.pdf",
            "Coordinación Académica"
        ));

        solicitudesMock.add(createSolicitud(
            "CERT-2026-002",
            "Sofia Bermúdez Castro",
            "1098432102",
            "11° - 01",
            "Certificado de notas",
            "Certificados Académicos",
            "Ingreso a procesos de selección universitaria",
            "2026-07-30",
            "Resuelto",
            "Adjunto certificado con el historial de notas por asignaturas y períodos académicos.",
            "Certificado_Notas_SofiaBermudez.pdf",
            "Secretaría Académica"
        ));

        solicitudesMock.add(createSolicitud(
            "CERT-2026-003",
            "Mateo Álvarez Restrepo",
            "1098432101",
            "11° - 01",
            "Paz y salvo",
            "Constancias Administrativas",
            "Verificación de estado de pago de pensión y materiales",
            "2026-07-31",
            "Pendiente",
            null,
            null,
            null
        ));

        solicitudesMock.add(createSolicitud(
            "CERT-2026-004",
            "Juan Diego Cárdenas",
            "1098432103",
            "11° - 01",
            "Certificado de comportamiento o conducta",
            "Constancias Administrativas",
            "Soporte de conducta escolar institucioanl",
            "2026-07-31",
            "Pendiente",
            null,
            null,
            null
        ));

        model.addAttribute("solicitudesMock", solicitudesMock);
        return "certificados/index";
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
