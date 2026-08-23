package com.siga.siga_iea.storage.repository;

import com.siga.siga_iea.storage.entity.Documento;
import com.siga.siga_iea.storage.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

    List<Documento> findByMatriculaId(UUID matriculaId);

    List<Documento> findByEstudianteId(UUID estudianteId);

    List<Documento> findByDocenteId(UUID docenteId);

    List<Documento> findByPersonalId(UUID personalId);

    Optional<Documento> findByMatriculaIdAndTipoDocumento(UUID matriculaId, TipoDocumento tipoDocumento);

    Optional<Documento> findByStorageKey(String storageKey);

    boolean existsByChecksum(String checksum);
}
