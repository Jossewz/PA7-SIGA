package com.siga.siga_iea.auditoria.repository;

import com.siga.siga_iea.auditoria.entity.AuditoriaCambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditoriaCambioRepository extends JpaRepository<AuditoriaCambio, UUID> {

    List<AuditoriaCambio> findByEntidadAndEntidadIdOrderByFechaDesc(String entidad, UUID entidadId);

    List<AuditoriaCambio> findByEntidadOrderByFechaDesc(String entidad);
}
