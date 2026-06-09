package com.plataforma.colegio.calificaciones.repository;

import com.plataforma.colegio.calificaciones.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalificacionesRepository extends JpaRepository<Calificacion, Long> {
}
