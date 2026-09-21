package com.siga.siga_iea.reportes.repository;

import com.siga.siga_iea.reportes.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, UUID> {
    List<Reporte> findByDocenteIdOrderByCreatedAtDesc(UUID docenteId);
    List<Reporte> findByEstudianteIdOrderByCreatedAtDesc(UUID estudianteId);
    List<Reporte> findAllByOrderByCreatedAtDesc();
    Optional<Reporte> findByCodigo(String codigo);
    List<Reporte> findByEstado(String estado);
}
