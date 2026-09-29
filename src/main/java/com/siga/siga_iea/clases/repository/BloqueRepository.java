package com.siga.siga_iea.clases.repository;

import com.siga.siga_iea.clases.entity.Bloque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BloqueRepository extends JpaRepository<Bloque, UUID> {
    List<Bloque> findByJornadaOrderByNumeroAsc(String jornada);
    Optional<Bloque> findByNumeroAndJornada(Integer numero, String jornada);
    List<Bloque> findAllByOrderByNumeroAsc();
}
