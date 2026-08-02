package com.siga.siga_iea.calificaciones.repository;

import com.siga.siga_iea.calificaciones.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalificacionesRepository extends JpaRepository<Calificacion, UUID> {

    List<Calificacion> findByEstudianteId(UUID estudianteId);

    Optional<Calificacion> findByEvaluacionIdAndEstudianteId(UUID evaluacionId, UUID estudianteId);

    @Query("SELECT c FROM Calificacion c WHERE c.evaluacion.cursoMateria.curso.id = :cursoId AND c.evaluacion.periodo = :periodo")
    List<Calificacion> findByCursoAndPeriodo(@Param("cursoId") UUID cursoId, @Param("periodo") Integer periodo);
}
