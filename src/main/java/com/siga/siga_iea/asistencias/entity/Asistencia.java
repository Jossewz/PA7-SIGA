package com.siga.siga_iea.asistencias.entity;

import com.siga.siga_iea.usuarios.entity.Estudiante;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "asistencias", uniqueConstraints = {
    @UniqueConstraint(name = "uk_asistencia_sesion_estudiante", columnNames = {"sesion_id", "estudiante_id"})
})
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionClase sesion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, length = 20)
    private String estado = "PRESENTE"; // 'PRESENTE', 'AUSENTE', 'TARDE', 'EXCUSADO'

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "PRESENTE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Asistencia() {}

    public Asistencia(SesionClase sesion, Estudiante estudiante, String estado) {
        this.sesion = sesion;
        this.estudiante = estudiante;
        this.estado = estado != null ? estado : "PRESENTE";
    }

    public Asistencia(SesionClase sesion, Estudiante estudiante, String estado, String observaciones) {
        this.sesion = sesion;
        this.estudiante = estudiante;
        this.estado = estado != null ? estado : "PRESENTE";
        this.observaciones = observaciones;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public SesionClase getSesion() { return sesion; }
    public void setSesion(SesionClase sesion) { this.sesion = sesion; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
