package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.EstudianteAcudiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstudianteAcudienteRepository extends JpaRepository<EstudianteAcudiente, UUID> {
    List<EstudianteAcudiente> findByEstudianteId(UUID estudianteId);
    List<EstudianteAcudiente> findByAcudienteId(UUID acudienteId);
    Optional<EstudianteAcudiente> findByEstudianteIdAndAcudienteId(UUID estudianteId, UUID acudienteId);
    Optional<EstudianteAcudiente> findByEstudianteIdAndEsPrincipalTrue(UUID estudianteId);
}
