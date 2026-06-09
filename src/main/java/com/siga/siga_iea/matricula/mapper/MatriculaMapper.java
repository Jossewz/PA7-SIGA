package com.siga.siga_iea.matricula.mapper;

import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.dto.MatriculaDTO;

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

