package com.siga.siga_iea.configuracion.repository;

import com.siga.siga_iea.configuracion.entity.ConfiguracionInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionInstitucionalRepository extends JpaRepository<ConfiguracionInstitucional, UUID> {
    Optional<ConfiguracionInstitucional> findFirstByOrderByUpdatedAtDesc();
}
