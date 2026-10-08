package com.siga.siga_iea.clases.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "bloques", uniqueConstraints = {
    @UniqueConstraint(name = "uk_bloque_numero_jornada", columnNames = {"numero", "jornada"})
})
public class Bloque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "jornada")
    private String jornada = "Mañana";

    @Column(name = "tipo")
    private String tipo = "CLASE"; // CLASE, DESCANSO

    @Column(name = "estado")
    private String estado = "Activo";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Bloque() {}

    public Bloque(Integer numero, String nombre, LocalTime horaInicio, LocalTime horaFin, String jornada, String tipo, String estado) {
        this.numero = numero;
        this.nombre = nombre;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.jornada = jornada != null ? jornada : "Mañana";
        this.tipo = tipo != null ? tipo : "CLASE";
        this.estado = estado != null ? estado : "Activo";
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getJornada() { return jornada; }
    public void setJornada(String jornada) { this.jornada = jornada; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
