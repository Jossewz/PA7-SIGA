package com.siga.siga_iea.clases.repository;

import com.siga.siga_iea.clases.entity.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, UUID> {
    Optional<Materia> findByNombre(String nombre);
}
