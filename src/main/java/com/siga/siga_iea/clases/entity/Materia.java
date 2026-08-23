package com.siga.siga_iea.clases.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "materias")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String nombre;

    @Column(columnDefinition = "VARCHAR")
    private String area;

    @Column(name = "intensidad_horaria")
    private Integer intensidadHoraria = 4;

    @Column(columnDefinition = "VARCHAR")
    private String estado = "Activo";

    public Materia() {}

    public Materia(String nombre, String area) {
        this.nombre = nombre;
        this.area = area;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Integer getIntensidadHoraria() { return intensidadHoraria; }
    public void setIntensidadHoraria(Integer intensidadHoraria) { this.intensidadHoraria = intensidadHoraria; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
