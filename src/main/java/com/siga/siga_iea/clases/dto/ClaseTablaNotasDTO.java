package com.siga.siga_iea.clases.dto;

import com.siga.siga_iea.calificaciones.entity.Evaluacion;
import com.siga.siga_iea.clases.entity.CursoEstudiante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * DTO que transporta los datos necesarios para renderizar el fragmento
 * HTMX de la tabla de notas y asistencias de un curso.
 */
public class ClaseTablaNotasDTO {

    private UUID cursoId;
    private UUID cursoMateriaId;
    private UUID materiaId;
    private String materiaNombre;
    private Integer periodo;
    private LocalDate fecha;
    private String diaSemana;
    private boolean tieneHorarioHoy;
    private String horarioBannerTexto;
    private List<CursoEstudiante> estudiantesCE = new ArrayList<>();
    private List<Evaluacion> evaluaciones = new ArrayList<>();
    private BigDecimal sumaPesos = BigDecimal.ZERO;
    private Map<UUID, Map<UUID, BigDecimal>> calificacionesMapa = new HashMap<>();
    private Map<UUID, String> asistenciasMapa = new HashMap<>();
    private Map<UUID, BigDecimal> notasFinales = new HashMap<>();

    public ClaseTablaNotasDTO() {
    }

    public UUID getCursoId() {
        return cursoId;
    }

    public void setCursoId(UUID cursoId) {
        this.cursoId = cursoId;
    }

    public UUID getCursoMateriaId() {
        return cursoMateriaId;
    }

    public void setCursoMateriaId(UUID cursoMateriaId) {
        this.cursoMateriaId = cursoMateriaId;
    }

    public UUID getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(UUID materiaId) {
        this.materiaId = materiaId;
    }

    public String getMateriaNombre() {
        return materiaNombre;
    }

    public void setMateriaNombre(String materiaNombre) {
        this.materiaNombre = materiaNombre;
    }

    public Integer getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Integer periodo) {
        this.periodo = periodo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public boolean isTieneHorarioHoy() {
        return tieneHorarioHoy;
    }

    public void setTieneHorarioHoy(boolean tieneHorarioHoy) {
        this.tieneHorarioHoy = tieneHorarioHoy;
    }

    public String getHorarioBannerTexto() {
        return horarioBannerTexto;
    }

    public void setHorarioBannerTexto(String horarioBannerTexto) {
        this.horarioBannerTexto = horarioBannerTexto;
    }

    public List<CursoEstudiante> getEstudiantesCE() {
        return estudiantesCE;
    }

    public void setEstudiantesCE(List<CursoEstudiante> estudiantesCE) {
        this.estudiantesCE = estudiantesCE;
    }

    public List<Evaluacion> getEvaluaciones() {
        return evaluaciones;
    }

    public void setEvaluaciones(List<Evaluacion> evaluaciones) {
        this.evaluaciones = evaluaciones;
    }

    public BigDecimal getSumaPesos() {
        return sumaPesos;
    }

    public void setSumaPesos(BigDecimal sumaPesos) {
        this.sumaPesos = sumaPesos;
    }

    public Map<UUID, Map<UUID, BigDecimal>> getCalificacionesMapa() {
        return calificacionesMapa;
    }

    public void setCalificacionesMapa(Map<UUID, Map<UUID, BigDecimal>> calificacionesMapa) {
        this.calificacionesMapa = calificacionesMapa;
    }

    public Map<UUID, String> getAsistenciasMapa() {
        return asistenciasMapa;
    }

    public void setAsistenciasMapa(Map<UUID, String> asistenciasMapa) {
        this.asistenciasMapa = asistenciasMapa;
    }

    public Map<UUID, BigDecimal> getNotasFinales() {
        return notasFinales;
    }

    public void setNotasFinales(Map<UUID, BigDecimal> notasFinales) {
        this.notasFinales = notasFinales;
    }
}
