package com.siga.siga_iea.clases.entity;

import com.siga.siga_iea.usuarios.entity.Estudiante;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "curso_estudiante")
public class CursoEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "curso_id", nullable = false)
    private Clase curso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(name = "ano_lectivo", columnDefinition = "VARCHAR", nullable = false)
    private String anoLectivo;

    public CursoEstudiante() {}

    public CursoEstudiante(Clase curso, Estudiante estudiante, String anoLectivo) {
        this.curso = curso;
        this.estudiante = estudiante;
        this.anoLectivo = anoLectivo;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Clase getCurso() { return curso; }
    public void setCurso(Clase curso) { this.curso = curso; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public String getAnoLectivo() { return anoLectivo; }
    public void setAnoLectivo(String anoLectivo) { this.anoLectivo = anoLectivo; }
}
