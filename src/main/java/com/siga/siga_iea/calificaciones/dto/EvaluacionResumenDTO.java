package com.siga.siga_iea.calificaciones.dto;

import java.math.BigDecimal;

public class EvaluacionResumenDTO {
    private String id;
    private String nombre;
    private String fechaDisplay;
    private BigDecimal peso;
    private BigDecimal nota;

    public EvaluacionResumenDTO() {}

    public EvaluacionResumenDTO(String id, String nombre, String fechaDisplay, BigDecimal peso, BigDecimal nota) {
        this.id = id;
        this.nombre = nombre;
        this.fechaDisplay = fechaDisplay;
        this.peso = peso;
        this.nota = nota;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFechaDisplay() { return fechaDisplay; }
    public void setFechaDisplay(String fechaDisplay) { this.fechaDisplay = fechaDisplay; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }
}
