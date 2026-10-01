package com.siga.siga_iea.clases.repository;

import com.siga.siga_iea.clases.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CursoRepository extends JpaRepository<Curso, UUID> {

    List<Curso> findByAnoLectivo(String anoLectivo);

    Optional<Curso> findByGradoAndGrupoAndAnoLectivo(String grado, String grupo, String anoLectivo);

    @Query("SELECT c FROM Curso c WHERE c.grado = :grado AND c.grupo = :grupo")
    Optional<Curso> findByGradoAndGrupo(@Param("grado") String grado, @Param("grupo") String grupo);

    List<Curso> findByDirectorId(UUID directorId);

    List<Curso> findByDirectorIdAndAnoLectivo(UUID directorId, String anoLectivo);
}
