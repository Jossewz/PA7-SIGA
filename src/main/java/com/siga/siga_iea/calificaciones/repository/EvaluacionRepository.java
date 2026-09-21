package com.siga.siga_iea.calificaciones.repository;

import com.siga.siga_iea.calificaciones.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, UUID> {
    @Query("SELECT e FROM Evaluacion e WHERE e.cursoMateria.id = :cursoMateriaId AND e.periodo = :periodo ORDER BY e.createdAt ASC, e.nombre ASC")
    List<Evaluacion> findByCursoMateriaIdAndPeriodo(@Param("cursoMateriaId") UUID cursoMateriaId, @Param("periodo") Integer periodo);

    @Query("SELECT e FROM Evaluacion e WHERE e.cursoMateria.id = :cursoMateriaId ORDER BY e.createdAt ASC, e.nombre ASC")
    List<Evaluacion> findByCursoMateriaId(@Param("cursoMateriaId") UUID cursoMateriaId);
}
