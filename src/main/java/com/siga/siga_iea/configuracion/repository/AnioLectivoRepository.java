package com.siga.siga_iea.configuracion.repository;

import com.siga.siga_iea.configuracion.entity.AnioLectivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnioLectivoRepository extends JpaRepository<AnioLectivo, UUID> {
    Optional<AnioLectivo> findByAnio(Integer anio);
    Optional<AnioLectivo> findByEsActualTrue();
    List<AnioLectivo> findAllByOrderByAnioDesc();
}
