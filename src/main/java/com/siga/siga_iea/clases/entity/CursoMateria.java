package com.siga.siga_iea.clases.entity;

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
    private Clase curso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @Column(name = "ano_lectivo", columnDefinition = "VARCHAR", nullable = false)
    private String anoLectivo;

    public CursoMateria() {}

    public CursoMateria(Clase curso, Materia materia, Docente docente, String anoLectivo) {
        this.curso = curso;
        this.materia = materia;
        this.docente = docente;
        this.anoLectivo = anoLectivo;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Clase getCurso() { return curso; }
    public void setCurso(Clase curso) { this.curso = curso; }

    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public String getAnoLectivo() { return anoLectivo; }
    public void setAnoLectivo(String anoLectivo) { this.anoLectivo = anoLectivo; }
}
