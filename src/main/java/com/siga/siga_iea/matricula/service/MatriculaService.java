package com.siga.siga_iea.matricula.service;

import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.CursoEstudiante;
import com.siga.siga_iea.clases.repository.CursoEstudianteRepository;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final CursoRepository cursoRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;

    public MatriculaService(MatriculaRepository matriculaRepository,
                            CursoRepository cursoRepository,
                            CursoEstudianteRepository cursoEstudianteRepository) {
        this.matriculaRepository = matriculaRepository;
        this.cursoRepository = cursoRepository;
        this.cursoEstudianteRepository = cursoEstudianteRepository;
    }

    @Transactional
    public Matricula guardar(Matricula matricula) {
        return matriculaRepository.save(matricula);
    }

    @Transactional(readOnly = true)
    public Optional<Matricula> buscarPorId(UUID id) {
        return matriculaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Matricula> buscarUltimaMatriculaEstudiante(UUID estudianteId) {
        return matriculaRepository.findTopByEstudianteIdOrderByFechaMatriculaDesc(estudianteId);
    }

    @Transactional(readOnly = true)
    public List<Matricula> buscarPorCurso(UUID cursoId) {
        return matriculaRepository.findByCursoId(cursoId);
    }

    /**
     * Aprueba la matrícula de un estudiante y vincula atómicamente el CursoEstudiante
     * en la misma transacción para garantizar coherencia académica.
     */
    @Transactional
    public Matricula aprobarMatricula(UUID matriculaId, UUID cursoId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula no encontrada: " + matriculaId));
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));

        matricula.setEstado("APROBADA");
        matricula.setCurso(curso);
        if (curso.getGrado() != null) {
            matricula.setGrado(curso.getGrado());
        }
        if (curso.getGrupo() != null) {
            matricula.setSalon(curso.getGrupo());
        }
        if (curso.getAnoLectivo() != null) {
            matricula.setAnoLectivo(curso.getAnoLectivo());
        }

        Estudiante estudiante = matricula.getEstudiante();
        if (estudiante != null) {
            estudiante.setEstado("Activo");
            String ano = matricula.getAnoLectivo() != null ? matricula.getAnoLectivo() : "2026";
            Optional<CursoEstudiante> existente = cursoEstudianteRepository.findByEstudianteIdAndAnoLectivo(estudiante.getId(), ano);
            if (existente.isPresent()) {
                CursoEstudiante ce = existente.get();
                ce.setCurso(curso);
                cursoEstudianteRepository.save(ce);
            } else {
                CursoEstudiante ce = new CursoEstudiante(curso, estudiante, ano);
                cursoEstudianteRepository.save(ce);
            }
        }

        return matriculaRepository.save(matricula);
    }

    /**
     * Sobrecarga para aprobar matrícula usando el curso ya asignado o buscando por grado/grupo.
     */
    @Transactional
    public Matricula aprobarMatricula(UUID matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula no encontrada: " + matriculaId));
        if (matricula.getCurso() != null) {
            return aprobarMatricula(matriculaId, matricula.getCurso().getId());
        }
        String ano = matricula.getAnoLectivo() != null ? matricula.getAnoLectivo() : "2026";
        String salon = matricula.getSalon() != null ? matricula.getSalon() : "01";
        String grado = matricula.getGrado();
        Optional<Curso> cursoOpt = cursoRepository.findByGradoAndGrupoAndAnoLectivo(grado, salon, ano);
        if (cursoOpt.isEmpty()) {
            cursoOpt = cursoRepository.findByGradoAndGrupo(grado, salon);
        }
        if (cursoOpt.isPresent()) {
            return aprobarMatricula(matriculaId, cursoOpt.get().getId());
        }
        matricula.setEstado("APROBADA");
        return matriculaRepository.save(matricula);
    }

    /**
     * Traslada a un estudiante a un nuevo curso, actualizando tanto la matrícula
     * administrativa como la asignación académica (CursoEstudiante) en la misma transacción.
     */
    @Transactional
    public Matricula trasladarEstudiante(UUID matriculaId, UUID nuevoCursoId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula no encontrada: " + matriculaId));
        Curso nuevoCurso = cursoRepository.findById(nuevoCursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + nuevoCursoId));

        matricula.setCurso(nuevoCurso);
        if (nuevoCurso.getGrado() != null) {
            matricula.setGrado(nuevoCurso.getGrado());
        }
        if (nuevoCurso.getGrupo() != null) {
            matricula.setSalon(nuevoCurso.getGrupo());
        }
        if (nuevoCurso.getAnoLectivo() != null) {
            matricula.setAnoLectivo(nuevoCurso.getAnoLectivo());
        }

        Estudiante estudiante = matricula.getEstudiante();
        if (estudiante != null) {
            String ano = matricula.getAnoLectivo() != null ? matricula.getAnoLectivo() : "2026";
            Optional<CursoEstudiante> existente = cursoEstudianteRepository.findByEstudianteIdAndAnoLectivo(estudiante.getId(), ano);
            if (existente.isPresent()) {
                CursoEstudiante ce = existente.get();
                ce.setCurso(nuevoCurso);
                cursoEstudianteRepository.save(ce);
            } else {
                CursoEstudiante ce = new CursoEstudiante(nuevoCurso, estudiante, ano);
                cursoEstudianteRepository.save(ce);
            }
        }

        return matriculaRepository.save(matricula);
    }
}
