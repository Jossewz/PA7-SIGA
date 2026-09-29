package com.siga.siga_iea.clases.repository;

import com.siga.siga_iea.clases.entity.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalonRepository extends JpaRepository<Salon, UUID> {
    Optional<Salon> findByCodigo(String codigo);
    List<Salon> findByEstado(String estado);
    List<Salon> findAllByOrderByCodigoAsc();
    boolean existsByCodigo(String codigo);
}
