package com.siga.siga_iea.usuarios.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "personal_administrativo")
public class PersonalAdministrativo {

    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    public PersonalAdministrativo() {
    }

    public PersonalAdministrativo(Usuario usuario) {
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
