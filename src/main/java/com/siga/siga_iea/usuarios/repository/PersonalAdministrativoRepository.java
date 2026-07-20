package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.PersonalAdministrativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalAdministrativoRepository extends JpaRepository<PersonalAdministrativo, UUID> {

    Optional<PersonalAdministrativo> findByNumeroDocumento(String numeroDocumento);

    @Query("SELECT p FROM PersonalAdministrativo p WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(p.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:cargo IS NULL OR :cargo = '' OR p.cargo = :cargo) AND " +
           "(:area IS NULL OR :area = '' OR p.area = :area) AND " +
           "(:estado IS NULL OR :estado = '' OR p.estado = :estado) " +
           "ORDER BY p.createdAt DESC")
    List<PersonalAdministrativo> searchPersonal(
            @Param("search") String search,
            @Param("cargo") String cargo,
            @Param("area") String area,
            @Param("estado") String estado
    );
}
