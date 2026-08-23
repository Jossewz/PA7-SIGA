package com.siga.siga_iea.certificados.repository;

import com.siga.siga_iea.certificados.entity.SolicitudCertificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudCertificadoRepository extends JpaRepository<SolicitudCertificado, UUID> {
    List<SolicitudCertificado> findByEstudianteIdOrderByCreatedAtDesc(UUID estudianteId);
    List<SolicitudCertificado> findAllByOrderByCreatedAtDesc();
    Optional<SolicitudCertificado> findByCodigo(String codigo);
    List<SolicitudCertificado> findByEstado(String estado);
}
