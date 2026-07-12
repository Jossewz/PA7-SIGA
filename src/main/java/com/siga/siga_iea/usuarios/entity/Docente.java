package com.siga.siga_iea.usuarios.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "docentes")
public class Docente {

    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    public Docente() {
    }

    public Docente(Usuario usuario) {
        this.usuario = usuario;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
