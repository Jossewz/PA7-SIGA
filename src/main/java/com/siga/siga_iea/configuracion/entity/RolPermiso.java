package com.siga.siga_iea.configuracion.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "rol_permisos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"rol", "modulo"})
})
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String rol;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String modulo;

    @Column(name = "nombre_modulo", columnDefinition = "VARCHAR", nullable = false)
    private String nombreModulo;

    @Column(columnDefinition = "VARCHAR")
    private String descripcion;

    @Column(name = "puede_acceder")
    private Boolean puedeAcceder = true;

    @Column(name = "puede_editar")
    private Boolean puedeEditar = true;

    @Column(name = "puede_eliminar")
    private Boolean puedeEliminar = false;

    public RolPermiso() {}

    public RolPermiso(String rol, String modulo, String nombreModulo, String descripcion, Boolean puedeAcceder, Boolean puedeEditar, Boolean puedeEliminar) {
        this.rol = rol;
        this.modulo = modulo;
        this.nombreModulo = nombreModulo;
        this.descripcion = descripcion;
        this.puedeAcceder = puedeAcceder;
        this.puedeEditar = puedeEditar;
        this.puedeEliminar = puedeEliminar;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getNombreModulo() { return nombreModulo; }
    public void setNombreModulo(String nombreModulo) { this.nombreModulo = nombreModulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getPuedeAcceder() { return puedeAcceder != null ? puedeAcceder : false; }
    public void setPuedeAcceder(Boolean puedeAcceder) { this.puedeAcceder = puedeAcceder; }

    public Boolean getPuedeEditar() { return puedeEditar != null ? puedeEditar : false; }
    public void setPuedeEditar(Boolean puedeEditar) { this.puedeEditar = puedeEditar; }

    public Boolean getPuedeEliminar() { return puedeEliminar != null ? puedeEliminar : false; }
    public void setPuedeEliminar(Boolean puedeEliminar) { this.puedeEliminar = puedeEliminar; }
}
