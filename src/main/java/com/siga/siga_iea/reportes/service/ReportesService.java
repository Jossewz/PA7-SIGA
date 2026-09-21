package com.siga.siga_iea.reportes.service;

import com.siga.siga_iea.reportes.entity.Reporte;
import com.siga.siga_iea.reportes.repository.ReporteRepository;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.PersonalAdministrativo;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import com.siga.siga_iea.usuarios.repository.PersonalAdministrativoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class ReportesService {

    private final ReporteRepository reporteRepository;
    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;
    private final PersonalAdministrativoRepository personalRepository;

    public ReportesService(ReporteRepository reporteRepository,
                           EstudianteRepository estudianteRepository,
                           DocenteRepository docenteRepository,
                           PersonalAdministrativoRepository personalRepository) {
        this.reporteRepository = reporteRepository;
        this.estudianteRepository = estudianteRepository;
        this.docenteRepository = docenteRepository;
        this.personalRepository = personalRepository;
    }

    public List<Reporte> listarTodos() {
        return reporteRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Reporte> listarPorDocente(UUID docenteId) {
        return reporteRepository.findByDocenteIdOrderByCreatedAtDesc(docenteId);
    }

    public List<Reporte> listarPorEstudiante(UUID estudianteId) {
        return reporteRepository.findByEstudianteIdOrderByCreatedAtDesc(estudianteId);
    }

    public List<Reporte> filtrarPorEstado(String estado) {
        if (estado == null || estado.isBlank() || "todos".equalsIgnoreCase(estado)) {
            return listarTodos();
        }
        return reporteRepository.findByEstado(estado);
    }

    public Optional<Reporte> buscarPorId(UUID id) {
        return reporteRepository.findById(id);
    }

    @Transactional
    public Reporte crearReporte(UUID estudianteId, UUID docenteId, String categoria, String razon, String descripcionRazon, String detalles) {
        Estudiante est = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        Docente doc = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado"));

        Reporte r = new Reporte();
        r.setCodigo("REP-2026-" + (100 + new Random().nextInt(900)));
        r.setEstudiante(est);
        r.setDocente(doc);
        r.setCategoria(categoria);
        r.setRazon(razon);
        r.setDescripcionRazon(descripcionRazon);
        r.setDetalles(detalles);
        r.setEstado("Pendiente");

        return reporteRepository.save(r);
    }

    @Transactional
    public Reporte atenderReporte(UUID reporteId, String decision, LocalDateTime fechaCitacion, Boolean requiereAcudiente, String observacionesAdmin, UUID adminId) {
        Reporte r = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));

        r.setEstado(decision != null ? decision : "Aceptado");
        r.setFechaCitacion(fechaCitacion);
        r.setRequiereAcudiente(requiereAcudiente != null ? requiereAcudiente : false);
        r.setObservacionesAdmin(observacionesAdmin);

        if (adminId != null) {
            personalRepository.findById(adminId).ifPresent(r::setAtendidoPor);
        }

        return reporteRepository.save(r);
    }
}
