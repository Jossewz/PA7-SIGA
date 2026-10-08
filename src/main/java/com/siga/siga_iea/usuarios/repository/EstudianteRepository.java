package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, UUID> {

    Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);

    Optional<Estudiante> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByNumeroDocumento(String numeroDocumento);

    @Query("SELECT e FROM Estudiante e WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(e.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.codigo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.numeroDocumento) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:estado IS NULL OR :estado = '' OR e.estado = :estado) " +
           "ORDER BY e.createdAt DESC")
    List<Estudiante> searchEstudiantes(@Param("search") String search, @Param("estado") String estado);

    @Query(value = "SELECT DISTINCT e FROM Estudiante e " +
           "LEFT JOIN CursoEstudiante ce ON ce.estudiante = e " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           " LOWER(e.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.codigo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.numeroDocumento) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:estado IS NULL OR :estado = '' OR e.estado = :estado) AND " +
           "(:nivel IS NULL OR :nivel = '' OR " +
           " (:nivel = 'PRIMARIA' AND ce.curso.grado IN ('1°', '2°', '3°', '4°', '5°')) OR " +
           " (:nivel = 'BACHILLERATO' AND ce.curso.grado IN ('6°', '7°', '8°', '9°', '10°', '11°'))) " +
           "ORDER BY e.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT e) FROM Estudiante e " +
           "LEFT JOIN CursoEstudiante ce ON ce.estudiante = e " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           " LOWER(e.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.codigo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.numeroDocumento) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:estado IS NULL OR :estado = '' OR e.estado = :estado) AND " +
           "(:nivel IS NULL OR :nivel = '' OR " +
           " (:nivel = 'PRIMARIA' AND ce.curso.grado IN ('1°', '2°', '3°', '4°', '5°')) OR " +
           " (:nivel = 'BACHILLERATO' AND ce.curso.grado IN ('6°', '7°', '8°', '9°', '10°', '11°')))")
    Page<Estudiante> searchEstudiantesPaginado(
            @Param("search") String search,
            @Param("estado") String estado,
            @Param("nivel") String nivel,
            Pageable pageable);
}
