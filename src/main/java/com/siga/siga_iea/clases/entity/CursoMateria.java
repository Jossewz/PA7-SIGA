package com.siga.siga_iea.clases.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siga.siga_iea.configuracion.entity.AnioLectivo;
import com.siga.siga_iea.usuarios.entity.Docente;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "curso_materia")
public class CursoMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_lectivo_id")
    private AnioLectivo anioLectivo;

    @Deprecated
    @Column(name = "ano_lectivo", columnDefinition = "VARCHAR", nullable = false)
    private String anoLectivo;

    public CursoMateria() {}

    public CursoMateria(Curso curso, Materia materia, Docente docente, AnioLectivo anioLectivo) {
        this.curso = curso;
        this.materia = materia;
        this.docente = docente;
        this.anioLectivo = anioLectivo;
        if (anioLectivo != null && anioLectivo.getAnio() != null) {
            this.anoLectivo = anioLectivo.getAnio().toString();
        }
    }

    @Deprecated
    public CursoMateria(Curso curso, Materia materia, Docente docente, String anoLectivo) {
        this.curso = curso;
        this.materia = materia;
        this.docente = docente;
        this.anoLectivo = anoLectivo;
        if (curso != null && curso.getAnioLectivo() != null) {
            this.anioLectivo = curso.getAnioLectivo();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) {
        this.curso = curso;
        if (this.anioLectivo == null && curso != null && curso.getAnioLectivo() != null) {
            this.setAnioLectivo(curso.getAnioLectivo());
        }
    }

    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    @JsonIgnore
    public AnioLectivo getAnioLectivo() { return anioLectivo; }
    public void setAnioLectivo(AnioLectivo anioLectivo) {
        this.anioLectivo = anioLectivo;
        if (anioLectivo != null && anioLectivo.getAnio() != null) {
            this.anoLectivo = anioLectivo.getAnio().toString();
        }
    }

    @PrePersist
    @PreUpdate
    public void syncDerivations() {
        if (this.anioLectivo != null && this.anioLectivo.getAnio() != null) {
            this.anoLectivo = this.anioLectivo.getAnio().toString();
        } else if (this.curso != null && this.curso.getAnioLectivo() != null) {
            this.anioLectivo = this.curso.getAnioLectivo();
            this.anoLectivo = this.anioLectivo.getAnio().toString();
        }
    }

    @Deprecated
    public String getAnoLectivo() {
        return anoLectivo;
    }

    @Deprecated
    public void setAnoLectivo(String anoLectivo) {
        if (this.anioLectivo == null) {
            this.anoLectivo = anoLectivo;
        }
    }
}
