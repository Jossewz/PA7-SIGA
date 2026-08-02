package com.siga.siga_iea.dashboard.service;

import com.siga.siga_iea.certificados.entity.SolicitudCertificado;
import com.siga.siga_iea.certificados.repository.SolicitudCertificadoRepository;
import com.siga.siga_iea.clases.entity.Clase;
import com.siga.siga_iea.clases.repository.ClaseRepository;
import com.siga.siga_iea.dashboard.dto.DashboardStatsDTO;
import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;
import com.siga.siga_iea.reportes.entity.Reporte;
import com.siga.siga_iea.reportes.repository.ReporteRepository;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DashboardService {

    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;
    private final ReporteRepository reporteRepository;
    private final SolicitudCertificadoRepository certificadoRepository;
    private final MatriculaRepository matriculaRepository;
    private final ClaseRepository claseRepository;

    public DashboardService(EstudianteRepository estudianteRepository,
                            DocenteRepository docenteRepository,
                            ReporteRepository reporteRepository,
                            SolicitudCertificadoRepository certificadoRepository,
                            MatriculaRepository matriculaRepository,
                            ClaseRepository claseRepository) {
        this.estudianteRepository = estudianteRepository;
        this.docenteRepository = docenteRepository;
        this.reporteRepository = reporteRepository;
        this.certificadoRepository = certificadoRepository;
        this.matriculaRepository = matriculaRepository;
        this.claseRepository = claseRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO obtenerEstadisticas() {
        long estudiantesCount = estudianteRepository.count();
        long docentesCount = docenteRepository.count();
        long totalReportes = reporteRepository.count();
        long totalMatriculas = matriculaRepository.count();
        long totalCursos = claseRepository.count();

        List<Reporte> reportesPendientes = reporteRepository.findByEstado("Pendiente");
        List<SolicitudCertificado> certsPendientes = certificadoRepository.findByEstado("Pendiente");

        long alertasCount = reportesPendientes.size() + certsPendientes.size();

        // Estudiantes que requieren atención
        List<Map<String, Object>> estudiantesAtencion = new ArrayList<>();
        for (Reporte r : reportesPendientes) {
            if (r.getEstudiante() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("nombre", r.getEstudiante().getNombreCompleto());
                map.put("motivo", r.getRazon() != null ? r.getRazon() : "Reporte pendiente");
                map.put("accion", "Atender");
                estudiantesAtencion.add(map);
            }
        }

        // Timeline de actividad (combinando matrículas, reportes y certificados)
        List<Map<String, Object>> actividadTimeline = new ArrayList<>();

        List<Matricula> matriculas = matriculaRepository.findAll();
        for (Matricula m : matriculas) {
            Map<String, Object> map = new HashMap<>();
            String nombre = m.getEstudiante() != null ? m.getEstudiante().getNombreCompleto() : "Estudiante";
            map.put("titulo", "Matrícula registrada: " + nombre);
            map.put("tiempo", m.getFechaMatricula() != null ? m.getFechaMatricula().toString() : "Reciente");
            actividadTimeline.add(map);
        }

        for (Reporte r : reportesPendientes) {
            Map<String, Object> map = new HashMap<>();
            String nombre = r.getEstudiante() != null ? r.getEstudiante().getNombreCompleto() : "Estudiante";
            map.put("titulo", "Reporte generado para " + nombre);
            map.put("tiempo", r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate().toString() : "Reciente");
            actividadTimeline.add(map);
        }

        for (SolicitudCertificado sc : certsPendientes) {
            Map<String, Object> map = new HashMap<>();
            String tipo = sc.getTipo() != null ? sc.getTipo() : "Certificado";
            map.put("titulo", "Solicitud de " + tipo);
            map.put("tiempo", sc.getCreatedAt() != null ? sc.getCreatedAt().toLocalDate().toString() : "Reciente");
            actividadTimeline.add(map);
        }

        // Alertas operativas
        List<Map<String, Object>> alertasOperativas = new ArrayList<>();
        if (!certsPendientes.isEmpty()) {
            Map<String, Object> a = new HashMap<>();
            a.put("titulo", certsPendientes.size() + " solicitudes de certificados pendientes de revisión");
            a.put("subtitulo", "Bandeja de Secretaría Académica");
            a.put("icono", "file-warning");
            a.put("tipo", "warning");
            alertasOperativas.add(a);
        }

        if (!reportesPendientes.isEmpty()) {
            Map<String, Object> a = new HashMap<>();
            a.put("titulo", reportesPendientes.size() + " reportes disciplinarios/académicos por atender");
            a.put("subtitulo", "Módulo de Coordinación / Rectoría");
            a.put("icono", "alert-triangle");
            a.put("tipo", "alert");
            alertasOperativas.add(a);
        }

        List<Clase> clasesDB = claseRepository.findAll();
        long clasesSinDirector = clasesDB.stream().filter(c -> c.getDirector() == null).count();
        if (clasesSinDirector > 0) {
            Map<String, Object> a = new HashMap<>();
            a.put("titulo", clasesSinDirector + " cursos sin director de grupo asignado");
            a.put("subtitulo", "Revisar la gestión de clases");
            a.put("icono", "user-x");
            a.put("tipo", "warning");
            alertasOperativas.add(a);
        }

        return DashboardStatsDTO.builder()
                .estudiantesActivos(estudiantesCount)
                .docentesActivos(docentesCount)
                .asistenciaHoy("0%")
                .alertasSistema(alertasCount)
                .totalReportes(totalReportes)
                .totalMatriculas(totalMatriculas)
                .totalCursos(totalCursos)
                .estudiantesAtencion(estudiantesAtencion)
                .actividadTimeline(actividadTimeline)
                .alertasOperativas(alertasOperativas)
                .build();
    }
}
