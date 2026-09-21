package com.siga.siga_iea.configuracion.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracion_institucional")
public class ConfiguracionInstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String nit;

    @Column(name = "nombre_inst", columnDefinition = "VARCHAR", nullable = false)
    private String nombreInst;

    @Column(name = "direccion_inst", columnDefinition = "VARCHAR")
    private String direccionInst;

    @Column(name = "telefono_inst", columnDefinition = "VARCHAR")
    private String telefonoInst;

    @Column(name = "correo_inst", columnDefinition = "VARCHAR")
    private String correoInst;

    @Column(name = "ano_lectivo", columnDefinition = "VARCHAR", nullable = false)
    private String anoLectivo = "2026";

    @Column(name = "rector_nombre", columnDefinition = "VARCHAR")
    private String rectorNombre;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public ConfiguracionInstitucional() {}

    public ConfiguracionInstitucional(String nit, String nombreInst, String direccionInst, String telefonoInst, String correoInst, String anoLectivo) {
        this.nit = nit;
        this.nombreInst = nombreInst;
        this.direccionInst = direccionInst;
        this.telefonoInst = telefonoInst;
        this.correoInst = correoInst;
        this.anoLectivo = anoLectivo;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getNombreInst() { return nombreInst; }
    public void setNombreInst(String nombreInst) { this.nombreInst = nombreInst; }

    public String getDireccionInst() { return direccionInst; }
    public void setDireccionInst(String direccionInst) { this.direccionInst = direccionInst; }

    public String getTelefonoInst() { return telefonoInst; }
    public void setTelefonoInst(String telefonoInst) { this.telefonoInst = telefonoInst; }

    public String getCorreoInst() { return correoInst; }
    public void setCorreoInst(String correoInst) { this.correoInst = correoInst; }

    public String getAnoLectivo() { return anoLectivo; }
    public void setAnoLectivo(String anoLectivo) { this.anoLectivo = anoLectivo; }

    public String getRectorNombre() { return rectorNombre; }
    public void setRectorNombre(String rectorNombre) { this.rectorNombre = rectorNombre; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
