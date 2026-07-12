package com.siga.siga_iea.usuarios.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String nombres;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String apellidos;

    @Column(columnDefinition = "VARCHAR", unique = true, nullable = false)
    private String email;

    @Column(columnDefinition = "VARCHAR")
    private String tipoDocumento;

    @Column(columnDefinition = "VARCHAR", unique = true)
    private String numeroDocumento;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String rol;

    public Usuario() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
