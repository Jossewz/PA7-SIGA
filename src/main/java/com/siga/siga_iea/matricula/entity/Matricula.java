package com.siga.siga_iea.matricula.entity;

import com.siga.siga_iea.usuarios.entity.Usuario;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDate;

@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String grado;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String anoLectivo;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String estado;

    @Column(name = "fecha_matricula")
    private LocalDate fechaMatricula;

    public Matricula() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Usuario estudiante) {
        this.estudiante = estudiante;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getAnoLectivo() {
        return anoLectivo;
    }

    public void setAnoLectivo(String anoLectivo) {
        this.anoLectivo = anoLectivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaMatricula() {
        return fechaMatricula;
    }

    public void setFechaMatricula(LocalDate fechaMatricula) {
        this.fechaMatricula = fechaMatricula;
    }
}
