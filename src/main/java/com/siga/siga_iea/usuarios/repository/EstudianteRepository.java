package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.Estudiante;
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
}
