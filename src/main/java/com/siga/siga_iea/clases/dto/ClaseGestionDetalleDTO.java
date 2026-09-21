package com.siga.siga_iea.clases.dto;

import java.util.*;

/**
 * DTO que transporta los datos requeridos para renderizar la vista
 * principal de gestión de curso (clases/detalle).
 */
public class ClaseGestionDetalleDTO {

    private String cursoId;
    private String codigoCurso;
    private String gradoCurso;
    private String directorCurso;
    private String jornadaCurso;
    private List<Map<String, Object>> estudiantesData = new ArrayList<>();
    private List<Map<String, String>> horariosData = new ArrayList<>();
    private boolean esAdmin;
    private String horarioBannerTexto;
    private ClaseTablaNotasDTO tablaNotasDTO;

    public ClaseGestionDetalleDTO() {
    }

    public String getCursoId() {
        return cursoId;
    }

    public void setCursoId(String cursoId) {
        this.cursoId = cursoId;
    }

    public String getCodigoCurso() {
        return codigoCurso;
    }

    public void setCodigoCurso(String codigoCurso) {
        this.codigoCurso = codigoCurso;
    }

    public String getGradoCurso() {
        return gradoCurso;
    }

    public void setGradoCurso(String gradoCurso) {
        this.gradoCurso = gradoCurso;
    }

    public String getDirectorCurso() {
        return directorCurso;
    }

    public void setDirectorCurso(String directorCurso) {
        this.directorCurso = directorCurso;
    }

    public String getJornadaCurso() {
        return jornadaCurso;
    }

    public void setJornadaCurso(String jornadaCurso) {
        this.jornadaCurso = jornadaCurso;
    }

    public List<Map<String, Object>> getEstudiantesData() {
        return estudiantesData;
    }

    public void setEstudiantesData(List<Map<String, Object>> estudiantesData) {
        this.estudiantesData = estudiantesData;
    }

    public List<Map<String, String>> getHorariosData() {
        return horariosData;
    }

    public void setHorariosData(List<Map<String, String>> horariosData) {
        this.horariosData = horariosData;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public String getHorarioBannerTexto() {
        return horarioBannerTexto;
    }

    public void setHorarioBannerTexto(String horarioBannerTexto) {
        this.horarioBannerTexto = horarioBannerTexto;
    }

    public ClaseTablaNotasDTO getTablaNotasDTO() {
        return tablaNotasDTO;
    }

    public void setTablaNotasDTO(ClaseTablaNotasDTO tablaNotasDTO) {
        this.tablaNotasDTO = tablaNotasDTO;
    }
}
