package com.siga.siga_iea.calificaciones.entity;

import com.siga.siga_iea.clases.entity.CursoMateria;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evaluaciones")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "curso_materia_id", nullable = false)
    private CursoMateria cursoMateria;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer periodo; // 1, 2, 3

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal peso; // e.g. 25.00

    private LocalDate fecha;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Evaluacion() {}

    public Evaluacion(CursoMateria cursoMateria, String nombre, Integer periodo, BigDecimal peso) {
        this.cursoMateria = cursoMateria;
        this.nombre = nombre;
        this.periodo = periodo;
        this.peso = peso;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CursoMateria getCursoMateria() { return cursoMateria; }
    public void setCursoMateria(CursoMateria cursoMateria) { this.cursoMateria = cursoMateria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getPeriodo() { return periodo; }
    public void setPeriodo(Integer periodo) { this.periodo = periodo; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
