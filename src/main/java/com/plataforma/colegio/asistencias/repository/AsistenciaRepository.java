package com.plataforma.colegio.asistencias.repository;

import com.plataforma.colegio.asistencias.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
}
