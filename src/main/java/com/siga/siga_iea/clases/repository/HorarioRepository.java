package com.siga.siga_iea.clases.repository;

import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, UUID> {

    List<Horario> findByCursoId(UUID cursoId);

    List<Horario> findByCursoIdAndMateriaId(UUID cursoId, UUID materiaId);

    List<Horario> findByDocenteId(UUID docenteId);

    List<Horario> findByCursoIdAndDocenteId(UUID cursoId, UUID docenteId);

    // ==========================================
    // CHOQUES POR DOCENTE (JOIN EXCLUSIVO CON CURSOMATERIA)
    // ==========================================
    @Query("SELECT DISTINCT h FROM Horario h " +
           "JOIN CursoMateria cm ON (cm.curso = h.curso AND cm.materia = h.materia AND cm.anoLectivo = :anoLectivo) " +
           "WHERE cm.docente.id = :docenteId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.curso.anoLectivo = :anoLectivo " +
           "AND h.horaInicio < :horaFin AND h.horaFin > :horaInicio")
    List<Horario> buscarSolapamientosDocente(
            @Param("docenteId") UUID docenteId,
            @Param("diaSemana") String diaSemana,
            @Param("anoLectivo") String anoLectivo,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    @Query("SELECT DISTINCT h FROM Horario h " +
           "JOIN CursoMateria cm ON (cm.curso = h.curso AND cm.materia = h.materia AND cm.anoLectivo = :anoLectivo) " +
           "WHERE cm.docente.id = :docenteId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.curso.anoLectivo = :anoLectivo " +
           "AND h.id != :excluirId " +
           "AND h.horaInicio < :horaFin AND h.horaFin > :horaInicio")
    List<Horario> buscarSolapamientosDocenteExcluyendoId(
            @Param("docenteId") UUID docenteId,
            @Param("diaSemana") String diaSemana,
            @Param("anoLectivo") String anoLectivo,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excluirId") UUID excluirId);

    // ==========================================
    // CHOQUES POR SALÓN
    // ==========================================
    @Query("SELECT h FROM Horario h WHERE " +
           "((:salonId IS NOT NULL AND h.salonEntidad.id = :salonId) OR " +
           " (:salonNombre IS NOT NULL AND :salonNombre <> '' AND LOWER(TRIM(h.salon)) = LOWER(TRIM(:salonNombre)))) " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.curso.anoLectivo = :anoLectivo " +
           "AND h.horaInicio < :horaFin AND h.horaFin > :horaInicio")
    List<Horario> buscarSolapamientosSalon(
            @Param("salonId") UUID salonId,
            @Param("salonNombre") String salonNombre,
            @Param("diaSemana") String diaSemana,
            @Param("anoLectivo") String anoLectivo,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    @Query("SELECT h FROM Horario h WHERE " +
           "((:salonId IS NOT NULL AND h.salonEntidad.id = :salonId) OR " +
           " (:salonNombre IS NOT NULL AND :salonNombre <> '' AND LOWER(TRIM(h.salon)) = LOWER(TRIM(:salonNombre)))) " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.curso.anoLectivo = :anoLectivo " +
           "AND h.id != :excluirId " +
           "AND h.horaInicio < :horaFin AND h.horaFin > :horaInicio")
    List<Horario> buscarSolapamientosSalonExcluyendoId(
            @Param("salonId") UUID salonId,
            @Param("salonNombre") String salonNombre,
            @Param("diaSemana") String diaSemana,
            @Param("anoLectivo") String anoLectivo,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excluirId") UUID excluirId);

    // ==========================================
    // CHOQUES POR CURSO
    // ==========================================
    @Query("SELECT h FROM Horario h WHERE h.curso.id = :cursoId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.horaInicio < :horaFin AND h.horaFin > :horaInicio")
    List<Horario> buscarSolapamientosCurso(
            @Param("cursoId") UUID cursoId,
            @Param("diaSemana") String diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    @Query("SELECT h FROM Horario h WHERE h.curso.id = :cursoId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.id != :excluirId " +
           "AND h.horaInicio < :horaFin AND h.horaFin > :horaInicio")
    List<Horario> buscarSolapamientosCursoExcluyendoId(
            @Param("cursoId") UUID cursoId,
            @Param("diaSemana") String diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excluirId") UUID excluirId);

    // ==========================================
    // CURSOS DEL DOCENTE POR DÍA (MI JORNADA)
    // ==========================================
    @Query("SELECT DISTINCT h.curso FROM Horario h " +
           "JOIN h.curso c " +
           "JOIN CursoMateria cm ON (cm.curso = c AND cm.materia = h.materia AND cm.anoLectivo = c.anoLectivo) " +
           "WHERE cm.docente.id = :docenteId AND LOWER(h.diaSemana) = LOWER(:diaSemana)")
    List<Curso> findCursosDocentePorDia(
            @Param("docenteId") UUID docenteId,
            @Param("diaSemana") String diaSemana);
}
