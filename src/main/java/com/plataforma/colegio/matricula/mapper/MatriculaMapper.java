package com.plataforma.colegio.matricula.mapper;

import com.plataforma.colegio.matricula.entity.Matricula;
import com.plataforma.colegio.matricula.dto.MatriculaDTO;

public class MatriculaMapper {
    public static MatriculaDTO toDTO(Matricula matricula) {
        MatriculaDTO dto = new MatriculaDTO();
        if (matricula != null) {
            dto.setId(matricula.getId());
            dto.setCurso(matricula.getCurso());
        }
        return dto;
    }
}
