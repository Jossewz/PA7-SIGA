package com.siga.siga_iea.clases.repository;

import com.siga.siga_iea.clases.entity.CursoMateria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CursoMateriaRepository extends JpaRepository<CursoMateria, UUID> {
    List<CursoMateria> findByCursoId(UUID cursoId);
    Optional<CursoMateria> findByCursoIdAndMateriaIdAndAnoLectivo(UUID cursoId, UUID materiaId, String anoLectivo);
}
