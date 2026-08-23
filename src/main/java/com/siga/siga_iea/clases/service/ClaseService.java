package com.siga.siga_iea.clases.service;

import com.siga.siga_iea.clases.entity.*;
import com.siga.siga_iea.clases.repository.*;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;

import java.time.LocalTime;
import java.util.*;

import com.siga.siga_iea.calificaciones.entity.Calificacion;
import com.siga.siga_iea.calificaciones.repository.CalificacionesRepository;

@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final MateriaRepository materiaRepository;
    private final CursoMateriaRepository cursoMateriaRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;
    private final HorarioRepository horarioRepository;
    private final DocenteRepository docenteRepository;
    private final EstudianteRepository estudianteRepository;
    private final MatriculaRepository matriculaRepository;
    private final CalificacionesRepository calificacionesRepository;

    public ClaseService(ClaseRepository claseRepository,
                        MateriaRepository materiaRepository,
                        CursoMateriaRepository cursoMateriaRepository,
                        CursoEstudianteRepository cursoEstudianteRepository,
                        HorarioRepository horarioRepository,
                        DocenteRepository docenteRepository,
                        EstudianteRepository estudianteRepository,
                        MatriculaRepository matriculaRepository,
                        CalificacionesRepository calificacionesRepository) {
        this.claseRepository = claseRepository;
        this.materiaRepository = materiaRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.cursoEstudianteRepository = cursoEstudianteRepository;
        this.horarioRepository = horarioRepository;
        this.docenteRepository = docenteRepository;
        this.estudianteRepository = estudianteRepository;
        this.matriculaRepository = matriculaRepository;
        this.calificacionesRepository = calificacionesRepository;
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
        String degreeNum = parts[0].replaceAll("[^0-9]", "");
        String grado = (degreeNum.isEmpty() ? "11" : degreeNum) + "°";
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

    @Transactional
    public Clase actualizarCurso(UUID cursoId, String grado, String jornada, Integer cupos, UUID directorId, String anoLectivo) {
        Clase c = claseRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        c.setGrado(grado);
        if (jornada != null && !jornada.isBlank()) c.setJornada(jornada);
        if (cupos != null && cupos > 0) c.setCuposMaximos(cupos);
        if (anoLectivo != null && !anoLectivo.isBlank()) c.setAnoLectivo(anoLectivo);

        if (directorId != null) {
            docenteRepository.findById(directorId).ifPresent(c::setDirector);
        } else {
            c.setDirector(null);
        }

        return claseRepository.save(c);
    }

    public List<Materia> listarTodasMaterias() {
        List<Materia> materias = materiaRepository.findAll();
        if (materias.isEmpty()) {
            materias = Arrays.asList(
                materiaRepository.save(new Materia("Matemáticas", "Ciencias Exactas")),
                materiaRepository.save(new Materia("Español y Literatura", "Humanidades")),
                materiaRepository.save(new Materia("Ciencias Naturales", "Ciencias Exactas")),
                materiaRepository.save(new Materia("Ciencias Sociales", "Ciencias Sociales")),
                materiaRepository.save(new Materia("Inglés Técnico", "Idiomas")),
                materiaRepository.save(new Materia("Educación Física", "Deportes")),
                materiaRepository.save(new Materia("Tecnología e Informática", "Tecnología")),
                materiaRepository.save(new Materia("Educación Artística", "Artes")),
                materiaRepository.save(new Materia("Filosofía", "Humanidades")),
                materiaRepository.save(new Materia("Ética y Valores", "Humanidades"))
            );
        }
        return materias;
    }

    public List<CursoEstudiante> listarEstudiantesDeCurso(UUID cursoId) {
        return cursoEstudianteRepository.findByCursoId(cursoId);
    }

    public List<Horario> listarHorariosDeCurso(UUID cursoId) {
        return horarioRepository.findByCursoId(cursoId);
    }

    @Transactional
    public void limpiarHorarioCurso(UUID cursoId) {
        List<Horario> existentes = horarioRepository.findByCursoId(cursoId);
        if (!existentes.isEmpty()) {
            horarioRepository.deleteAll(existentes);
        }
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
    public Horario guardarHorarioBloque(UUID cursoId, String diaSemana, UUID materiaId, UUID docenteId, String horaInicioStr, String horaFinStr, String salon) {
        Clase curso = claseRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        LocalTime inicio = (horaInicioStr != null && !horaInicioStr.isBlank()) ? LocalTime.parse(horaInicioStr) : LocalTime.of(7, 0);
        LocalTime fin = (horaFinStr != null && !horaFinStr.isBlank()) ? LocalTime.parse(horaFinStr) : LocalTime.of(8, 30);

        Horario h = new Horario();
        h.setCurso(curso);
        h.setDiaSemana(diaSemana);
        h.setHoraInicio(inicio);
        h.setHoraFin(fin);
        h.setSalon(salon != null && !salon.isBlank() ? salon : "Aula 101");

        if (materiaId != null) {
            materiaRepository.findById(materiaId).ifPresent(h::setMateria);
        }
        if (docenteId != null) {
            docenteRepository.findById(docenteId).ifPresent(h::setDocente);
        }

        return horarioRepository.save(h);
    }

    @Transactional
    public void matricularEstudianteEnCurso(UUID cursoId, UUID estudianteId, String anoLectivo) {
        Clase c = claseRepository.findById(cursoId).orElseThrow();
        Estudiante e = estudianteRepository.findById(estudianteId).orElseThrow();

        if (e.getEstado() != null && "Graduado".equalsIgnoreCase(e.getEstado())) {
            throw new IllegalArgumentException("No se puede asignar al estudiante " + e.getNombreCompleto() + ": Los estudiantes con estado 'Graduado' no pueden ser vinculados a cursos académicos.");
        }

        Optional<CursoEstudiante> existing = cursoEstudianteRepository.findByCursoIdAndEstudianteIdAndAnoLectivo(cursoId, estudianteId, anoLectivo);
        if (existing.isEmpty()) {
            CursoEstudiante ce = new CursoEstudiante(c, e, anoLectivo);
            cursoEstudianteRepository.save(ce);
        }
    }

    @Transactional
    public int mapearEstudiantesMatriculados(UUID cursoId) {
        Clase curso = claseRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        String grado = curso.getGrado();
        String gradoLiso = grado != null ? grado.replaceAll("[^0-9]", "") : "11";
        String anoLectivo = curso.getAnoLectivo() != null ? curso.getAnoLectivo() : "2026";
        int cuposMaximos = curso.getCuposMaximos() != null ? curso.getCuposMaximos() : 35;

        List<CursoEstudiante> inscritosActuales = cursoEstudianteRepository.findByCursoId(cursoId);
        int disponibles = cuposMaximos - inscritosActuales.size();
        if (disponibles <= 0) return 0;

        Set<Matricula> candidatosMatricula = new LinkedHashSet<>();
        candidatosMatricula.addAll(matriculaRepository.filterMatriculas(grado, null));
        candidatosMatricula.addAll(matriculaRepository.filterMatriculas(gradoLiso, null));
        candidatosMatricula.addAll(matriculaRepository.filterMatriculas(gradoLiso + "°", null));

        List<Matricula> listaMatriculas = new ArrayList<>(candidatosMatricula);
        List<Estudiante> candidatosEstudiantes = new ArrayList<>();
        if (!listaMatriculas.isEmpty()) {
            for (Matricula m : listaMatriculas) {
                if (m.getEstudiante() != null) candidatosEstudiantes.add(m.getEstudiante());
            }
        } else {
            candidatosEstudiantes.addAll(estudianteRepository.findAll());
        }

        Collections.shuffle(candidatosEstudiantes);

        int asignados = 0;
        for (Estudiante e : candidatosEstudiantes) {
            if (asignados >= disponibles) break;

            // EXCLUIR ESTUDIANTES GRADUADOS: Los estudiantes egresados no pueden volver a pertenecer a una clase
            if (e.getEstado() != null && "Graduado".equalsIgnoreCase(e.getEstado())) {
                continue;
            }

            boolean yaTieneCursoEnAno = cursoEstudianteRepository.existsByEstudianteIdAndAnoLectivo(e.getId(), anoLectivo);
            if (!yaTieneCursoEnAno) {
                CursoEstudiante ce = new CursoEstudiante(curso, e, anoLectivo);
                cursoEstudianteRepository.save(ce);
                asignados++;
            }
        }

        return asignados;
    }

    private double calcularNotaPromedioEstudiante(UUID estudianteId, UUID cursoId) {
        List<Calificacion> cals = calificacionesRepository.findByEstudianteId(estudianteId);
        if (cals.isEmpty()) {
            return 0.0; // Si no hay calificaciones en BD ni ingresadas, la nota por defecto es 0.00 (Reprobado)
        }

        double suma = 0.0;
        int count = 0;
        for (Calificacion c : cals) {
            if (c.getEvaluacion() != null && c.getEvaluacion().getCursoMateria() != null 
                    && cursoId.equals(c.getEvaluacion().getCursoMateria().getCurso().getId())) {
                if (c.getNota() != null) {
                    suma += c.getNota().doubleValue();
                    count++;
                }
            }
        }
        return count > 0 ? (suma / count) : 0.0;
    }

    @Transactional
    public String promoverEstudiantesAprobados(UUID cursoId, String notasJson) {
        Clase cursoOrigen = claseRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso origen no encontrado"));

        List<CursoEstudiante> inscritos = cursoEstudianteRepository.findByCursoId(cursoId);
        if (inscritos.isEmpty()) {
            return "El curso " + cursoOrigen.getCodigoCurso() + " no contiene estudiantes para promover.";
        }

        Map<String, Double> notasMap = new HashMap<>();
        if (notasJson != null && !notasJson.isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                notasMap = mapper.readValue(notasJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Double>>() {});
            } catch (Exception ignored) {}
        }

        String gradoActual = cursoOrigen.getGrado();
        String grupoActual = cursoOrigen.getGrupo();
        String siguienteGrado = calcularSiguienteGrado(gradoActual);

        int promovidos = 0;
        int repitentes = 0;
        List<String> nombresNoAprobados = new ArrayList<>();
        List<CursoEstudiante> aEliminarDeOrigen = new ArrayList<>();

        // CASO ESPECIAL: Si es Grado 11 (o superior) -> Graduación
        if ("Graduado".equalsIgnoreCase(siguienteGrado)) {
            for (CursoEstudiante ce : inscritos) {
                Estudiante e = ce.getEstudiante();
                if (e == null) continue;

                double promedio = 0.0;
                String estIdStr = e.getId().toString();
                if (notasMap.containsKey(estIdStr)) {
                    promedio = notasMap.get(estIdStr);
                } else {
                    promedio = calcularNotaPromedioEstudiante(e.getId(), cursoOrigen.getId());
                }

                boolean aprobado = (promedio >= 3.0) && !"Retirado".equalsIgnoreCase(e.getEstado());

                if (aprobado) {
                    e.setEstado("Graduado");
                    estudianteRepository.save(e);
                    aEliminarDeOrigen.add(ce);
                    promovidos++;
                } else {
                    repitentes++;
                    nombresNoAprobados.add(e.getNombreCompleto() + " (Nota: " + String.format("%.2f", promedio) + ")");
                }
            }

            if (!aEliminarDeOrigen.isEmpty()) {
                cursoEstudianteRepository.deleteAll(aEliminarDeOrigen);
            }

            if (repitentes > 0) {
                return String.format("Proceso de Graduación Concluido: Se han graduado satisfactoriamente %d estudiante(s) con calificación igual o superior a 3.0. " +
                        "Un total de %d estudiante(s) (%s) no alcanzaron la nota mínima aprobatoria (menos de 3.0) y NO se graduarán en esta cohorte, permaneciendo asignados al curso %s.",
                        promovidos, repitentes, String.join(", ", nombresNoAprobados), cursoOrigen.getCodigoCurso());
            } else {
                return String.format("Proceso de Graduación Concluido Con Éxito: El 100%% de los estudiantes (%d inscritos) del curso %s obtuvieron calificaciones aprobatorias (≥ 3.0) y han sido promovidos al estado de Graduados. El salón queda disponible para el siguiente ciclo académico.",
                        promovidos, cursoOrigen.getCodigoCurso());
            }
        }

        // PARA OTROS GRADOS (1° a 10°): Buscar el curso destino existente con el mismo grupo (ej. 10-01 -> 11-01)
        Clase cursoDestino = claseRepository.findAll().stream()
                .filter(c -> siguienteGrado.equalsIgnoreCase(c.getGrado()) && grupoActual.equalsIgnoreCase(c.getGrupo()))
                .findFirst()
                .orElseGet(() -> {
                    Clase nuevo = new Clase();
                    nuevo.setGrado(siguienteGrado);
                    nuevo.setGrupo(grupoActual);
                    nuevo.setJornada(cursoOrigen.getJornada());
                    nuevo.setCuposMaximos(cursoOrigen.getCuposMaximos());
                    nuevo.setDirector(cursoOrigen.getDirector());
                    return claseRepository.save(nuevo);
                });

        for (CursoEstudiante ce : inscritos) {
            Estudiante e = ce.getEstudiante();
            if (e == null) continue;

            double promedio = 0.0;
            String estIdStr = e.getId().toString();
            if (notasMap.containsKey(estIdStr)) {
                promedio = notasMap.get(estIdStr);
            } else {
                promedio = calcularNotaPromedioEstudiante(e.getId(), cursoOrigen.getId());
            }

            boolean aprobado = (promedio >= 3.0) && !"Retirado".equalsIgnoreCase(e.getEstado());

            if (aprobado) {
                boolean yaEnDestino = cursoEstudianteRepository.existsByCursoIdAndEstudianteId(cursoDestino.getId(), e.getId());
                if (!yaEnDestino) {
                    CursoEstudiante nuevoCE = new CursoEstudiante(cursoDestino, e, cursoOrigen.getAnoLectivo());
                    cursoEstudianteRepository.save(nuevoCE);
                }
                aEliminarDeOrigen.add(ce);
                promovidos++;
            } else {
                repitentes++;
                nombresNoAprobados.add(e.getNombreCompleto() + " (Nota: " + String.format("%.2f", promedio) + ")");
            }
        }

        // Vaciar a los promovidos de este salón para que ingresen los de la cohorte inferior
        if (!aEliminarDeOrigen.isEmpty()) {
            cursoEstudianteRepository.deleteAll(aEliminarDeOrigen);
        }

        if (repitentes > 0) {
            return String.format("Proceso de Promoción Académica Ejecutado: %d estudiante(s) con desempeño aprobatorio (≥ 3.0) fueron promovidos al curso %s (%s). " +
                    "Se informa que %d estudiante(s) (%s) obtuvieron nota final inferior a 3.0 y no cumplen el requisito para avanzar, por lo que permanecerán en el curso %s compartiendo aula con la nueva cohorte.",
                    promovidos, cursoDestino.getCodigoCurso(), siguienteGrado, repitentes, String.join(", ", nombresNoAprobados), cursoOrigen.getCodigoCurso());
        } else {
            return String.format("Proceso de Promoción Académica Concluido Con Éxito: El 100%% de los estudiantes (%d inscritos) alcanzaron o superaron la nota mínima de 3.0 y avanzan satisfactoriamente al curso %s (%s).",
                    promovidos, cursoDestino.getCodigoCurso(), siguienteGrado);
        }
    }

    public String promoverEstudiantesAprobados(UUID cursoId) {
        return promoverEstudiantesAprobados(cursoId, null);
    }

    @Transactional
    public void eliminarCurso(UUID cursoId) {
        List<CursoEstudiante> estudiantes = cursoEstudianteRepository.findByCursoId(cursoId);
        cursoEstudianteRepository.deleteAll(estudiantes);

        List<Horario> horarios = horarioRepository.findByCursoId(cursoId);
        horarioRepository.deleteAll(horarios);

        claseRepository.deleteById(cursoId);
    }

    private String calcularSiguienteGrado(String gradoActual) {
        if (gradoActual == null) return "1°";
        String numStr = gradoActual.replaceAll("[^0-9]", "");
        if (numStr.isEmpty()) return "1°";
        int num = Integer.parseInt(numStr);
        if (num >= 11) return "Graduado";
        return (num + 1) + "°";
    }
}
