package com.siga.siga_iea.calificaciones.repository;

import com.siga.siga_iea.calificaciones.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, UUID> {
    List<Evaluacion> findByCursoMateriaIdAndPeriodo(UUID cursoMateriaId, Integer periodo);
    List<Evaluacion> findByCursoMateriaId(UUID cursoMateriaId);
}
