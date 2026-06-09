package com.plataforma.colegio.clases.repository;

import com.plataforma.colegio.clases.entity.Clase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseRepository extends JpaRepository<Clase, Long> {
}
