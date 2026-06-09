package com.plataforma.colegio.matricula.repository;

import com.plataforma.colegio.matricula.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
}
