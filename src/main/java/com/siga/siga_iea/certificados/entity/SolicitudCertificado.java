package com.siga.siga_iea.certificados.entity;

import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.PersonalAdministrativo;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "solicitudes_certificado")
public class SolicitudCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "VARCHAR", nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String tipo;

    @Column(columnDefinition = "VARCHAR")
    private String categoria;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "ano_lectivo", columnDefinition = "VARCHAR")
    private String anoLectivo;

    @Column(name = "grado_referencia", columnDefinition = "VARCHAR")
    private String gradoReferencia;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String estado = "Pendiente"; // Pendiente, Resuelto

    @Column(name = "mensaje_respuesta", columnDefinition = "TEXT")
    private String mensajeRespuesta;

    @Column(name = "archivo_adjunto_key", columnDefinition = "VARCHAR")
    private String archivoAdjuntoKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondido_por")
    private PersonalAdministrativo respondidoPor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public SolicitudCertificado() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getAnoLectivo() { return anoLectivo; }
    public void setAnoLectivo(String anoLectivo) { this.anoLectivo = anoLectivo; }

    public String getGradoReferencia() { return gradoReferencia; }
    public void setGradoReferencia(String gradoReferencia) { this.gradoReferencia = gradoReferencia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMensajeRespuesta() { return mensajeRespuesta; }
    public void setMensajeRespuesta(String mensajeRespuesta) { this.mensajeRespuesta = mensajeRespuesta; }

    public String getArchivoAdjuntoKey() { return archivoAdjuntoKey; }
    public void setArchivoAdjuntoKey(String archivoAdjuntoKey) { this.archivoAdjuntoKey = archivoAdjuntoKey; }

    public PersonalAdministrativo getRespondidoPor() { return respondidoPor; }
    public void setRespondidoPor(PersonalAdministrativo respondidoPor) { this.respondidoPor = respondidoPor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
