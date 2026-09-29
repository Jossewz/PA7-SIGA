package com.siga.siga_iea.matricula.entity;

import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.storage.entity.Documento;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String grado;

    @Column(columnDefinition = "VARCHAR")
    private String salon = "01";

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String anoLectivo;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String estado;

    @Column(name = "fecha_matricula")
    private LocalDate fechaMatricula;

    @Column(name = "autoriza_tratamiento_datos")
    private Boolean autorizaTratamientoDatos = true;

    @Column(name = "fecha_autorizacion_datos")
    private LocalDateTime fechaAutorizacionDatos;

    @OneToMany(mappedBy = "matricula", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Documento> documentos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaAutorizacionDatos == null) {
            fechaAutorizacionDatos = LocalDateTime.now();
        }
    }

    public Matricula() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Boolean getAutorizaTratamientoDatos() {
        return autorizaTratamientoDatos;
    }

    public void setAutorizaTratamientoDatos(Boolean autorizaTratamientoDatos) {
        this.autorizaTratamientoDatos = autorizaTratamientoDatos;
    }

    public LocalDateTime getFechaAutorizacionDatos() {
        return fechaAutorizacionDatos;
    }

    public void setFechaAutorizacionDatos(LocalDateTime fechaAutorizacionDatos) {
        this.fechaAutorizacionDatos = fechaAutorizacionDatos;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getSalon() {
        return salon != null ? salon : "01";
    }

    public void setSalon(String salon) {
        this.salon = salon;
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

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }
}
