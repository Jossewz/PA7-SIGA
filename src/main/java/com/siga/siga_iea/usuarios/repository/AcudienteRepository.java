package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.Acudiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcudienteRepository extends JpaRepository<Acudiente, UUID> {
    Optional<Acudiente> findByNumeroDocumento(String numeroDocumento);
}
