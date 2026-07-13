package com.siga.siga_iea.matricula.service;

import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatriculaService {
    private final MatriculaRepository matriculaRepository;

    public MatriculaService(MatriculaRepository matriculaRepository) {
        this.matriculaRepository = matriculaRepository;
    }

    @Transactional
    public Matricula guardar(Matricula matricula) {
        return matriculaRepository.save(matricula);
    }
}
