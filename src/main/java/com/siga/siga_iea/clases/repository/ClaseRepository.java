package com.siga.siga_iea.clases.repository;

import com.siga.siga_iea.clases.entity.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, UUID> {

    List<Clase> findByAnoLectivo(String anoLectivo);

    Optional<Clase> findByGradoAndGrupoAndAnoLectivo(String grado, String grupo, String anoLectivo);

    @Query("SELECT c FROM Clase c WHERE c.grado = :grado AND c.grupo = :grupo")
    Optional<Clase> findByGradoAndGrupo(@Param("grado") String grado, @Param("grupo") String grupo);
}
