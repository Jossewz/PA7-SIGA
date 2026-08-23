package com.siga.siga_iea.matricula.repository;

import com.siga.siga_iea.matricula.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, UUID> {

    Optional<Matricula> findTopByEstudianteIdOrderByFechaMatriculaDesc(UUID estudianteId);

    List<Matricula> findByEstudianteId(UUID estudianteId);

    @Query("SELECT m FROM Matricula m JOIN FETCH m.estudiante e WHERE " +
           "(:grado IS NULL OR :grado = '' OR m.grado = :grado) AND " +
           "(:estado IS NULL OR :estado = '' OR m.estado = :estado) " +
           "ORDER BY m.fechaMatricula DESC")
    List<Matricula> filterMatriculas(@Param("grado") String grado, @Param("estado") String estado);
}
