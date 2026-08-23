package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, UUID> {

    Optional<Docente> findByNumeroDocumento(String numeroDocumento);

    @Query("SELECT d FROM Docente d WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(d.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.numeroDocumento) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:estado IS NULL OR :estado = '' OR d.estado = :estado) " +
           "ORDER BY d.createdAt DESC")
    List<Docente> searchDocentes(@Param("search") String search, @Param("estado") String estado);
}
