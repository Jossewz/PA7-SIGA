package com.siga.siga_iea.asistencias.entity;

import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.CursoMateria;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.usuarios.entity.Docente;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "sesiones_clase", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sesion_curso_fecha_hora", columnNames = {"curso_id", "fecha", "hora_inicio"})
})
public class SesionClase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "curso_materia_id")
    private CursoMateria cursoMateria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "horario_id")
    private Horario horario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false, length = 20)
    private String estado = "DICTADA"; // 'DICTADA', 'CANCELADA', 'REEMPLAZO'

    @Column(nullable = false, length = 20)
    private String tipo = "ASIGNATURA"; // 'ASIGNATURA', 'JORNADA'

    @Column(length = 255)
    private String tema;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "DICTADA";
        }
        if (this.tipo == null || this.tipo.isBlank()) {
            this.tipo = "ASIGNATURA";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public SesionClase() {}

    public SesionClase(Curso curso, CursoMateria cursoMateria, Horario horario, Docente docente,
                       LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String tipo) {
        this.curso = curso;
        this.cursoMateria = cursoMateria;
        this.horario = horario;
        this.docente = docente;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.tipo = tipo != null ? tipo : "ASIGNATURA";
        this.estado = "DICTADA";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public CursoMateria getCursoMateria() { return cursoMateria; }
    public void setCursoMateria(CursoMateria cursoMateria) { this.cursoMateria = cursoMateria; }

    public Horario getHorario() { return horario; }
    public void setHorario(Horario horario) { this.horario = horario; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
