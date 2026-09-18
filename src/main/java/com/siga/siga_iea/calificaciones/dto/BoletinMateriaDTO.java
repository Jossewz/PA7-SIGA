package com.siga.siga_iea.calificaciones.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BoletinMateriaDTO {
    private String cursoMateriaId;
    private String materiaId;
    private String materiaNombre;
    private BigDecimal periodo1Nota;
    private BigDecimal periodo2Nota;
    private BigDecimal periodo3Nota;
    private BigDecimal notaDefinitiva;
    private List<EvaluacionResumenDTO> evaluacionesP1 = new ArrayList<>();
    private List<EvaluacionResumenDTO> evaluacionesP2 = new ArrayList<>();
    private List<EvaluacionResumenDTO> evaluacionesP3 = new ArrayList<>();

    public BoletinMateriaDTO() {}

    public BoletinMateriaDTO(String cursoMateriaId, String materiaId, String materiaNombre) {
        this.cursoMateriaId = cursoMateriaId;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
    }

    public String getCursoMateriaId() { return cursoMateriaId; }
    public void setCursoMateriaId(String cursoMateriaId) { this.cursoMateriaId = cursoMateriaId; }

    public String getMateriaId() { return materiaId; }
    public void setMateriaId(String materiaId) { this.materiaId = materiaId; }

    public String getMateriaNombre() { return materiaNombre; }
    public void setMateriaNombre(String materiaNombre) { this.materiaNombre = materiaNombre; }

    public BigDecimal getPeriodo1Nota() { return periodo1Nota; }
    public void setPeriodo1Nota(BigDecimal periodo1Nota) { this.periodo1Nota = periodo1Nota; }

    public BigDecimal getPeriodo2Nota() { return periodo2Nota; }
    public void setPeriodo2Nota(BigDecimal periodo2Nota) { this.periodo2Nota = periodo2Nota; }

    public BigDecimal getPeriodo3Nota() { return periodo3Nota; }
    public void setPeriodo3Nota(BigDecimal periodo3Nota) { this.periodo3Nota = periodo3Nota; }

    public BigDecimal getNotaDefinitiva() { return notaDefinitiva; }
    public void setNotaDefinitiva(BigDecimal notaDefinitiva) { this.notaDefinitiva = notaDefinitiva; }

    public List<EvaluacionResumenDTO> getEvaluacionesP1() { return evaluacionesP1; }
    public void setEvaluacionesP1(List<EvaluacionResumenDTO> evaluacionesP1) { this.evaluacionesP1 = evaluacionesP1; }

    public List<EvaluacionResumenDTO> getEvaluacionesP2() { return evaluacionesP2; }
    public void setEvaluacionesP2(List<EvaluacionResumenDTO> evaluacionesP2) { this.evaluacionesP2 = evaluacionesP2; }

    public List<EvaluacionResumenDTO> getEvaluacionesP3() { return evaluacionesP3; }
    public void setEvaluacionesP3(List<EvaluacionResumenDTO> evaluacionesP3) { this.evaluacionesP3 = evaluacionesP3; }
}
