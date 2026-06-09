package com.siga.siga_iea.matricula.repository;

import com.siga.siga_iea.matricula.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
}

