package com.siga.siga_iea.configuracion.repository;

import com.siga.siga_iea.configuracion.entity.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, UUID> {
    List<PeriodoAcademico> findAllByOrderByNumeroPeriodoAsc();
    Optional<PeriodoAcademico> findByNumeroPeriodo(Integer numeroPeriodo);
}
