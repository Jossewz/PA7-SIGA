package com.siga.siga_iea.clases.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siga.siga_iea.clases.enums.GradoAcademico;
import com.siga.siga_iea.configuracion.entity.AnioLectivo;
import com.siga.siga_iea.usuarios.entity.Docente;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "cursos")
public class Curso {

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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_lectivo_id")
    private AnioLectivo anioLectivo;

    @Deprecated
    @Column(name = "ano_lectivo", columnDefinition = "VARCHAR", nullable = false)
    private String anoLectivo;

    @Column(columnDefinition = "VARCHAR")
    private String estado = "Activo";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        syncDerivations();
    }

    @PreUpdate
    protected void onUpdate() {
        syncDerivations();
    }

    public Curso() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getGrado() {
        return grado;
    }

    /**
     * Asigna el grado académico validándolo contra el catálogo institucional cerrado {@link GradoAcademico}.
     * 
     * @param grado Nombre del grado.
     * @throws IllegalArgumentException si el valor es nulo, vacío o no reconocido.
     */
    public void setGrado(String grado) {
        GradoAcademico g = GradoAcademico.from(grado);
        this.grado = g.getNombre();
    }

    public GradoAcademico getGradoAcademico() {
        return grado != null ? GradoAcademico.find(grado).orElse(null) : null;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getCodigoCurso() {
        String grupoStr = (grupo != null && !grupo.isBlank()) ? grupo : "01";
        if (grado != null && !grado.isBlank()) {
            Optional<GradoAcademico> gOpt = GradoAcademico.find(grado);
            if (gOpt.isPresent()) {
                return gOpt.get().getCodigoPrefijo() + "-" + grupoStr;
            }
            String num = grado.replaceAll("[^0-9]", "");
            if (!num.isEmpty()) {
                return num + "-" + grupoStr;
            }
        }
        return "11-" + grupoStr;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public Integer getCuposMaximos() {
        return cuposMaximos != null ? cuposMaximos : 35;
    }

    public void setCuposMaximos(Integer cuposMaximos) {
        this.cuposMaximos = cuposMaximos;
    }

    public Docente getDirector() {
        return director;
    }

    public void setDirector(Docente director) {
        this.director = director;
    }

    @JsonIgnore
    public AnioLectivo getAnioLectivo() {
        return anioLectivo;
    }

    public void setAnioLectivo(AnioLectivo anioLectivo) {
        this.anioLectivo = anioLectivo;
        if (anioLectivo != null && anioLectivo.getAnio() != null) {
            this.anoLectivo = anioLectivo.getAnio().toString();
        }
    }

    public void syncDerivations() {
        if (this.anioLectivo != null && this.anioLectivo.getAnio() != null) {
            this.anoLectivo = this.anioLectivo.getAnio().toString();
        }
    }

    @Deprecated
    public String getAnoLectivo() {
        return anoLectivo;
    }

    @Deprecated
    public void setAnoLectivo(String anoLectivo) {
        if (this.anioLectivo == null) {
            this.anoLectivo = anoLectivo;
        }
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
