package com.siga.siga_iea.configuracion.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "periodos_academicos", uniqueConstraints = {
    @UniqueConstraint(name = "uk_periodo_anio_numero", columnNames = {"anio_lectivo_id", "numero_periodo"})
})
public class PeriodoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_lectivo_id", nullable = false)
    private AnioLectivo anioLectivo;

    @Column(name = "numero_periodo", nullable = false)
    private Integer numeroPeriodo;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String nombre;

    @Column(name = "peso_porcentaje", precision = 5, scale = 2, nullable = false)
    private BigDecimal pesoPorcentaje;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(columnDefinition = "VARCHAR")
    private String estado = "Activo";

    public PeriodoAcademico() {}

    public PeriodoAcademico(AnioLectivo anioLectivo, Integer numeroPeriodo, String nombre, BigDecimal pesoPorcentaje, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.anioLectivo = anioLectivo;
        this.numeroPeriodo = numeroPeriodo;
        this.nombre = nombre;
        this.pesoPorcentaje = pesoPorcentaje;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public PeriodoAcademico(Integer numeroPeriodo, String nombre, BigDecimal pesoPorcentaje, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.numeroPeriodo = numeroPeriodo;
        this.nombre = nombre;
        this.pesoPorcentaje = pesoPorcentaje;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AnioLectivo getAnioLectivo() { return anioLectivo; }
    public void setAnioLectivo(AnioLectivo anioLectivo) { this.anioLectivo = anioLectivo; }

    public Integer getNumeroPeriodo() { return numeroPeriodo; }
    public void setNumeroPeriodo(Integer numeroPeriodo) { this.numeroPeriodo = numeroPeriodo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPesoPorcentaje() { return pesoPorcentaje; }
    public void setPesoPorcentaje(BigDecimal pesoPorcentaje) { this.pesoPorcentaje = pesoPorcentaje; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
