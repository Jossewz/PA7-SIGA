package com.siga.siga_iea.configuracion.repository;

import com.siga.siga_iea.configuracion.entity.AnioLectivo;
import com.siga.siga_iea.configuracion.entity.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, UUID> {

    List<PeriodoAcademico> findAllByOrderByNumeroPeriodoAsc();

    List<PeriodoAcademico> findByAnioLectivoOrderByNumeroPeriodoAsc(AnioLectivo anioLectivo);

    List<PeriodoAcademico> findByAnioLectivoIdOrderByNumeroPeriodoAsc(UUID anioLectivoId);

    Optional<PeriodoAcademico> findByAnioLectivoIdAndNumeroPeriodo(UUID anioLectivoId, Integer numeroPeriodo);

    @Query("SELECT p FROM PeriodoAcademico p WHERE (p.anioLectivo IS NOT NULL AND p.anioLectivo.esActual = true) AND p.numeroPeriodo = :numeroPeriodo")
    Optional<PeriodoAcademico> findByNumeroPeriodo(@Param("numeroPeriodo") Integer numeroPeriodo);

    @Query("SELECT p FROM PeriodoAcademico p WHERE (p.anioLectivo IS NOT NULL AND p.anioLectivo.esActual = true) ORDER BY p.numeroPeriodo ASC")
    List<PeriodoAcademico> findPeriodosAnioActual();
}
