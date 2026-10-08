package com.siga.siga_iea.auditoria.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auditoria_cambios")
public class AuditoriaCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "entidad", nullable = false, length = 50)
    private String entidad;

    @Column(name = "entidad_id", nullable = false)
    private UUID entidadId;

    @Column(name = "campo", nullable = false, length = 50)
    private String campo;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "usuario_email", length = 100)
    private String usuarioEmail;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public AuditoriaCambio() {
        this.fecha = LocalDateTime.now();
    }

    public AuditoriaCambio(String entidad, UUID entidadId, String campo, String valorAnterior,
                           String valorNuevo, UUID usuarioId, String usuarioEmail) {
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.usuarioId = usuarioId;
        this.usuarioEmail = usuarioEmail;
        this.fecha = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public UUID getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(UUID entidadId) {
        this.entidadId = entidadId;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
