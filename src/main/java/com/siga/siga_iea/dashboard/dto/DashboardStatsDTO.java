package com.siga.siga_iea.dashboard.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDTO {
    private long estudiantesActivos;
    private long docentesActivos;
    private String asistenciaHoy;
    private long alertasSistema;
    private long totalReportes;
    private long totalMatriculas;
    private long totalCursos;
    private List<Map<String, Object>> estudiantesAtencion;
    private List<Map<String, Object>> actividadTimeline;
    private List<Map<String, Object>> alertasOperativas;

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(long estudiantesActivos, long docentesActivos, String asistenciaHoy, long alertasSistema,
                             long totalReportes, long totalMatriculas, long totalCursos,
                             List<Map<String, Object>> estudiantesAtencion,
                             List<Map<String, Object>> actividadTimeline,
                             List<Map<String, Object>> alertasOperativas) {
        this.estudiantesActivos = estudiantesActivos;
        this.docentesActivos = docentesActivos;
        this.asistenciaHoy = asistenciaHoy;
        this.alertasSistema = alertasSistema;
        this.totalReportes = totalReportes;
        this.totalMatriculas = totalMatriculas;
        this.totalCursos = totalCursos;
        this.estudiantesAtencion = estudiantesAtencion;
        this.actividadTimeline = actividadTimeline;
        this.alertasOperativas = alertasOperativas;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long estudiantesActivos;
        private long docentesActivos;
        private String asistenciaHoy;
        private long alertasSistema;
        private long totalReportes;
        private long totalMatriculas;
        private long totalCursos;
        private List<Map<String, Object>> estudiantesAtencion;
        private List<Map<String, Object>> actividadTimeline;
        private List<Map<String, Object>> alertasOperativas;

        public Builder estudiantesActivos(long val) { this.estudiantesActivos = val; return this; }
        public Builder docentesActivos(long val) { this.docentesActivos = val; return this; }
        public Builder asistenciaHoy(String val) { this.asistenciaHoy = val; return this; }
        public Builder alertasSistema(long val) { this.alertasSistema = val; return this; }
        public Builder totalReportes(long val) { this.totalReportes = val; return this; }
        public Builder totalMatriculas(long val) { this.totalMatriculas = val; return this; }
        public Builder totalCursos(long val) { this.totalCursos = val; return this; }
        public Builder estudiantesAtencion(List<Map<String, Object>> val) { this.estudiantesAtencion = val; return this; }
        public Builder actividadTimeline(List<Map<String, Object>> val) { this.actividadTimeline = val; return this; }
        public Builder alertasOperativas(List<Map<String, Object>> val) { this.alertasOperativas = val; return this; }

        public DashboardStatsDTO build() {
            return new DashboardStatsDTO(estudiantesActivos, docentesActivos, asistenciaHoy, alertasSistema,
                    totalReportes, totalMatriculas, totalCursos, estudiantesAtencion, actividadTimeline, alertasOperativas);
        }
    }

    public long getEstudiantesActivos() { return estudiantesActivos; }
    public void setEstudiantesActivos(long estudiantesActivos) { this.estudiantesActivos = estudiantesActivos; }

    public long getDocentesActivos() { return docentesActivos; }
    public void setDocentesActivos(long docentesActivos) { this.docentesActivos = docentesActivos; }

    public String getAsistenciaHoy() { return asistenciaHoy; }
    public void setAsistenciaHoy(String asistenciaHoy) { this.asistenciaHoy = asistenciaHoy; }

    public long getAlertasSistema() { return alertasSistema; }
    public void setAlertasSistema(long alertasSistema) { this.alertasSistema = alertasSistema; }

    public long getTotalReportes() { return totalReportes; }
    public void setTotalReportes(long totalReportes) { this.totalReportes = totalReportes; }

    public long getTotalMatriculas() { return totalMatriculas; }
    public void setTotalMatriculas(long totalMatriculas) { this.totalMatriculas = totalMatriculas; }

    public long getTotalCursos() { return totalCursos; }
    public void setTotalCursos(long totalCursos) { this.totalCursos = totalCursos; }

    public List<Map<String, Object>> getEstudiantesAtencion() { return estudiantesAtencion; }
    public void setEstudiantesAtencion(List<Map<String, Object>> estudiantesAtencion) { this.estudiantesAtencion = estudiantesAtencion; }

    public List<Map<String, Object>> getActividadTimeline() { return actividadTimeline; }
    public void setActividadTimeline(List<Map<String, Object>> actividadTimeline) { this.actividadTimeline = actividadTimeline; }

    public List<Map<String, Object>> getAlertasOperativas() { return alertasOperativas; }
    public void setAlertasOperativas(List<Map<String, Object>> alertasOperativas) { this.alertasOperativas = alertasOperativas; }
}
