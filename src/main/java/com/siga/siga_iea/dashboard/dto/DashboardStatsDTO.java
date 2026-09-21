package com.siga.siga_iea.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
