package com.siga.siga_iea.configuracion.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "escala_desempeno")
public class EscalaDesempeno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "VARCHAR", nullable = false, unique = true)
    private String codigo;

    @Column(columnDefinition = "VARCHAR", nullable = false)
    private String nombre;

    @Column(name = "nota_minima", precision = 3, scale = 2, nullable = false)
    private BigDecimal notaMinima;

    @Column(name = "nota_maxima", precision = 3, scale = 2, nullable = false)
    private BigDecimal notaMaxima;

    @Column(name = "color_badge", columnDefinition = "VARCHAR", nullable = false)
    private String colorBadge;

    @Column(name = "color_hex", columnDefinition = "VARCHAR", nullable = false)
    private String colorHex;

    @Column(nullable = false)
    private Integer orden;

    public EscalaDesempeno() {}

    public EscalaDesempeno(String codigo, String nombre, BigDecimal notaMinima, BigDecimal notaMaxima, String colorBadge, String colorHex, Integer orden) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.notaMinima = notaMinima;
        this.notaMaxima = notaMaxima;
        this.colorBadge = colorBadge;
        this.colorHex = colorHex;
        this.orden = orden;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getNotaMinima() { return notaMinima; }
    public void setNotaMinima(BigDecimal notaMinima) { this.notaMinima = notaMinima; }

    public BigDecimal getNotaMaxima() { return notaMaxima; }
    public void setNotaMaxima(BigDecimal notaMaxima) { this.notaMaxima = notaMaxima; }

    public String getColorBadge() { return colorBadge; }
    public void setColorBadge(String colorBadge) { this.colorBadge = colorBadge; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
