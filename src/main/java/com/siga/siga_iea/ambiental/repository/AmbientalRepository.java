package com.siga.siga_iea.ambiental.repository;

import com.siga.siga_iea.ambiental.entity.ActividadAmbiental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmbientalRepository extends JpaRepository<ActividadAmbiental, Long> {
}

