package com.siga.siga_iea.configuracion.repository;

import com.siga.siga_iea.configuracion.entity.EscalaDesempeno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscalaDesempenoRepository extends JpaRepository<EscalaDesempeno, UUID> {
    List<EscalaDesempeno> findAllByOrderByOrdenAsc();
    Optional<EscalaDesempeno> findByCodigo(String codigo);
}
