package com.plataforma.colegio.ambiental.repository;

import com.plataforma.colegio.ambiental.entity.ActividadAmbiental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmbientalRepository extends JpaRepository<ActividadAmbiental, Long> {
}
