package com.siga.siga_iea.clases.service;

import com.siga.siga_iea.clases.entity.*;
import com.siga.siga_iea.clases.repository.*;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final MateriaRepository materiaRepository;
    private final CursoMateriaRepository cursoMateriaRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;
    private final HorarioRepository horarioRepository;
    private final DocenteRepository docenteRepository;
    private final EstudianteRepository estudianteRepository;

    public ClaseService(ClaseRepository claseRepository,
                        MateriaRepository materiaRepository,
                        CursoMateriaRepository cursoMateriaRepository,
                        CursoEstudianteRepository cursoEstudianteRepository,
                        HorarioRepository horarioRepository,
                        DocenteRepository docenteRepository,
                        EstudianteRepository estudianteRepository) {
        this.claseRepository = claseRepository;
        this.materiaRepository = materiaRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.cursoEstudianteRepository = cursoEstudianteRepository;
        this.horarioRepository = horarioRepository;
        this.docenteRepository = docenteRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public List<Clase> listarTodosLosCursos() {
        return claseRepository.findAll();
    }

    public List<Clase> listarCursosPorAno(String anoLectivo) {
        return claseRepository.findByAnoLectivo(anoLectivo);
    }

    public Optional<Clase> buscarPorId(UUID id) {
        return claseRepository.findById(id);
    }

    public Optional<Clase> buscarPorCodigo(String codigo, String anoLectivo) {
        if (codigo == null || !codigo.contains("-")) return Optional.empty();
        String[] parts = codigo.split("-");
        String grado = parts[0] + "°";
        String grupo = parts[1];
        return claseRepository.findByGradoAndGrupoAndAnoLectivo(grado, grupo, anoLectivo);
    }

    @Transactional
    public Clase guardarCurso(Clase clase) {
        return claseRepository.save(clase);
    }

    @Transactional
    public Clase crearCurso(String grado, String grupo, String jornada, Integer cupos, UUID directorId, String anoLectivo) {
        Clase c = new Clase();
        c.setGrado(grado);
        c.setGrupo(grupo != null && !grupo.isBlank() ? grupo : "01");
        c.setJornada(jornada != null ? jornada : "Mañana");
        c.setCuposMaximos(cupos != null ? cupos : 35);
        c.setAnoLectivo(anoLectivo != null ? anoLectivo : "2026");

        if (directorId != null) {
            docenteRepository.findById(directorId).ifPresent(c::setDirector);
        }
        return claseRepository.save(c);
    }

    public List<CursoEstudiante> listarEstudiantesDeCurso(UUID cursoId) {
        return cursoEstudianteRepository.findByCursoId(cursoId);
    }

    public List<Horario> listarHorariosDeCurso(UUID cursoId) {
        return horarioRepository.findByCursoId(cursoId);
    }

    @Transactional
    public Horario agregarHorario(UUID cursoId, UUID materiaId, String diaSemana, LocalTime horaInicio, LocalTime horaFin, String salon) {
        Clase curso = claseRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        Horario h = new Horario();
        h.setCurso(curso);
        h.setDiaSemana(diaSemana);
        h.setHoraInicio(horaInicio);
        h.setHoraFin(horaFin);
        h.setSalon(salon);

        if (materiaId != null) {
            materiaRepository.findById(materiaId).ifPresent(h::setMateria);
        }

        return horarioRepository.save(h);
    }

    @Transactional
    public void matricularEstudianteEnCurso(UUID cursoId, UUID estudianteId, String anoLectivo) {
        Clase c = claseRepository.findById(cursoId).orElseThrow();
        Estudiante e = estudianteRepository.findById(estudianteId).orElseThrow();

        Optional<CursoEstudiante> existing = cursoEstudianteRepository.findByCursoIdAndEstudianteIdAndAnoLectivo(cursoId, estudianteId, anoLectivo);
        if (existing.isEmpty()) {
            CursoEstudiante ce = new CursoEstudiante(c, e, anoLectivo);
            cursoEstudianteRepository.save(ce);
        }
    }
}
