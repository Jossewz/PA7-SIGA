package com.siga.siga_iea.clases.entity;

import com.siga.siga_iea.usuarios.entity.Docente;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cursos")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String grado;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String grupo = "01";

    @Column(columnDefinition = "VARCHAR")
    private String jornada = "Mañana";

    @Column(name = "cupos_maximos")
    private Integer cuposMaximos = 35;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "director_id")
    private Docente director;

    @Column(name = "ano_lectivo", columnDefinition = "VARCHAR", nullable = false)
    private String anoLectivo;

    @Column(columnDefinition = "VARCHAR")
    private String estado = "Activo";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Clase() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getGrado() { return grado; }
    public void setGrado(String grado) { 
        if (grado == null) {
            this.grado = "11°";
        } else {
            String num = grado.replaceAll("[^0-9]", "");
            this.grado = num.isEmpty() ? "11°" : num + "°";
        }
    }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public String getCodigoCurso() { 
        String num = grado != null ? grado.replaceAll("[^0-9]", "") : "11";
        return num + "-" + (grupo != null ? grupo : "01");
    }

    public String getJornada() { return jornada; }
    public void setJornada(String jornada) { this.jornada = jornada; }

    public Integer getCuposMaximos() { return cuposMaximos != null ? cuposMaximos : 35; }
    public void setCuposMaximos(Integer cuposMaximos) { this.cuposMaximos = cuposMaximos; }

    public Docente getDirector() { return director; }
    public void setDirector(Docente director) { this.director = director; }

    public String getAnoLectivo() { return anoLectivo; }
    public void setAnoLectivo(String anoLectivo) { this.anoLectivo = anoLectivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
