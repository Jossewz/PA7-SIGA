package com.siga.siga_iea.usuarios.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "estudiante_acudiente", uniqueConstraints = {
    @UniqueConstraint(name = "uk_estudiante_acudiente", columnNames = {"estudiante_id", "acudiente_id"})
})
public class EstudianteAcudiente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "acudiente_id", nullable = false)
    private Acudiente acudiente;

    @Column(name = "es_principal")
    private Boolean esPrincipal = false;

    @Column(name = "parentesco", columnDefinition = "VARCHAR")
    private String parentesco;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public EstudianteAcudiente() {}

    public EstudianteAcudiente(Estudiante estudiante, Acudiente acudiente, Boolean esPrincipal, String parentesco) {
        this.estudiante = estudiante;
        this.acudiente = acudiente;
        this.esPrincipal = esPrincipal != null ? esPrincipal : false;
        this.parentesco = parentesco;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public Acudiente getAcudiente() { return acudiente; }
    public void setAcudiente(Acudiente acudiente) { this.acudiente = acudiente; }

    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }

    public String getParentesco() { return parentesco; }
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
