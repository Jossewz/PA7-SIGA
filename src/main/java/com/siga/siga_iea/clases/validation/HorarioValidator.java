package com.siga.siga_iea.clases.validation;

import com.siga.siga_iea.clases.dto.HorarioDto;
import com.siga.siga_iea.clases.entity.Bloque;
import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.clases.repository.BloqueRepository;
import com.siga.siga_iea.clases.repository.CursoMateriaRepository;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.clases.repository.HorarioRepository;
import com.siga.siga_iea.clases.repository.MateriaRepository;
import com.siga.siga_iea.configuracion.repository.AnioLectivoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Validador desacoplado para la programación de horarios flexibles (estilo Google Calendar).
 * 
 * Responsabilidades:
 * 1. Coherencia horaria: hora_fin > hora_inicio (rechaza igualdad y orden inverso).
 * 2. Límites de jornada del curso (Curso.jornada): configurable desde Bloque o application.properties, con tolerancia explícita.
 * 3. Choques por docente: join Horario -> CursoMateria -> docente_id en el mismo día y año lectivo.
 * 4. Choques por salón: mismo espacio físico en el mismo día y año lectivo.
 * 5. Choques por curso: mismo grupo de estudiantes en el mismo día.
 * 6. Intensidad horaria: genera advertencia (no error) si se supera la intensidad semanal de la materia.
 * 
 * LIMITACIÓN CONOCIDA (CONDICIÓN DE CARRERA):
 * La validación a nivel de servicio tiene una pequeña ventana de concurrencia si dos usuarios guardan solapamientos
 * al mismo milisegundo exacto. Para los volúmenes del colegio (aprox. 3 usuarios administrativos) es completamente
 * segura y práctica. En producción de alta concurrencia, la restricción definitiva es EXCLUDE USING gist en PostgreSQL
 * (no soportada por H2 en perfiles de test).
 */
@Component
public class HorarioValidator {

    @Value("${siga.jornada.manana.inicio:07:00}")
    private String configMananaInicio = "07:00";

    @Value("${siga.jornada.manana.fin:12:00}")
    private String configMananaFin = "12:00";

    @Value("${siga.jornada.tarde.inicio:13:00}")
    private String configTardeInicio = "13:00";

    @Value("${siga.jornada.tarde.fin:18:00}")
    private String configTardeFin = "18:00";

    @Value("${siga.jornada.tolerancia-minutos:0}")
    private int configToleranciaMinutos = 0;

    private final HorarioRepository horarioRepository;
    private final CursoRepository cursoRepository;
    private final MateriaRepository materiaRepository;
    private final CursoMateriaRepository cursoMateriaRepository;
    private final BloqueRepository bloqueRepository;
    private final AnioLectivoRepository anioLectivoRepository;

    public HorarioValidator(
            HorarioRepository horarioRepository,
            CursoRepository cursoRepository,
            MateriaRepository materiaRepository,
            CursoMateriaRepository cursoMateriaRepository,
            BloqueRepository bloqueRepository,
            AnioLectivoRepository anioLectivoRepository
    ) {
        this.horarioRepository = horarioRepository;
        this.cursoRepository = cursoRepository;
        this.materiaRepository = materiaRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.bloqueRepository = bloqueRepository;
        this.anioLectivoRepository = anioLectivoRepository;
    }

    /**
     * Regla matemática de solapamiento entre dos franjas [iniA, finA) y [iniB, finB):
     * iniA < finB AND finA > iniB.
     * Bordes contiguos (ej. 08:40 y 08:40) NO se solapan.
     */
    public static boolean seSolapan(LocalTime iniA, LocalTime finA, LocalTime iniB, LocalTime finB) {
        return iniA.isBefore(finB) && finA.isAfter(iniB);
    }

    public record LimitesJornada(LocalTime inicio, LocalTime fin) {}

    public record ValidacionResultado(
            boolean valido,
            List<String> advertencias
    ) {
        public boolean tieneAdvertencias() {
            return advertencias != null && !advertencias.isEmpty();
        }
    }

    /**
     * Resuelve los límites de jornada configurables para una jornada dada.
     */
    public LimitesJornada obtenerLimitesJornada(String jornada) {
        String jNorm = jornada != null ? jornada.trim().toLowerCase() : "mañana";

        // 1. Si existen bloques marcados como 'LIMITE_JORNADA', usarlos como fuente prioritaria
        try {
            List<Bloque> limitesDb = bloqueRepository.findByTipoIgnoreCase("LIMITE_JORNADA");
            for (Bloque b : limitesDb) {
                if (b.getJornada() != null && b.getJornada().trim().equalsIgnoreCase(jNorm)) {
                    return new LimitesJornada(b.getHoraInicio(), b.getHoraFin());
                }
            }
        } catch (Exception ignored) {}

        // 2. Si no, tomar de propiedades configurables institucionales
        if (jNorm.contains("tar")) {
            return new LimitesJornada(
                    LocalTime.parse(configTardeInicio),
                    LocalTime.parse(configTardeFin)
            );
        }
        return new LimitesJornada(
                LocalTime.parse(configMananaInicio),
                LocalTime.parse(configMananaFin)
        );
    }

    /**
     * Valida un horario con tolerancia configurable por defecto.
     */
    public ValidacionResultado validar(HorarioDto dto, UUID horarioIdExcluido) {
        return validar(dto, horarioIdExcluido, Duration.ofMinutes(configToleranciaMinutos));
    }

    /**
     * Valida un horario con un parámetro explícito de tolerancia.
     */
    public ValidacionResultado validar(HorarioDto dto, UUID horarioIdExcluido, Duration tolerancia) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del horario no pueden ser nulos");
        }
        if (dto.cursoId() == null) {
            throw new IllegalArgumentException("El curso es obligatorio");
        }
        if (dto.horaInicio() == null || dto.horaFin() == null) {
            throw new IllegalArgumentException("La hora de inicio y fin son obligatorias");
        }

        // 1. hora_fin > hora_inicio (rechaza igualdad y orden inverso)
        if (!dto.horaFin().isAfter(dto.horaInicio())) {
            throw new IllegalArgumentException("La hora de fin (" + dto.horaFin() +
                    ") debe ser estrictamente posterior a la hora de inicio (" + dto.horaInicio() + ")");
        }

        // 2. Límites de jornada del curso (Curso.jornada) con tolerancia explícita
        Curso curso = cursoRepository.findById(dto.cursoId())
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado con ID: " + dto.cursoId()));

        LimitesJornada limites = obtenerLimitesJornada(curso.getJornada());
        Duration tol = tolerancia != null ? tolerancia : Duration.ZERO;
        LocalTime inicioPermitido = limites.inicio().minus(tol);
        LocalTime finPermitido = limites.fin().plus(tol);

        if (dto.horaInicio().isBefore(inicioPermitido) || dto.horaFin().isAfter(finPermitido)) {
            throw new IllegalArgumentException("El horario (" + dto.horaInicio() + " - " + dto.horaFin() +
                    ") excede los límites configurados para la jornada " + curso.getJornada() +
                    " (" + limites.inicio() + " - " + limites.fin() + ")");
        }

        // 3. Resolución del año lectivo (del curso o del AnioLectivo actual)
        String anoLectivo = curso.getAnoLectivo();
        if (anoLectivo == null || anoLectivo.isBlank()) {
            anoLectivo = anioLectivoRepository.findByEsActualTrue()
                    .map(a -> String.valueOf(a.getAnio()))
                    .orElse("2026");
        }

        // 4. Resolución del docente efectivo (si no viene en DTO, buscar en CursoMateria)
        UUID docenteEfectivoId = dto.docenteId();
        if (docenteEfectivoId == null && dto.materiaId() != null) {
            var cmOpt = cursoMateriaRepository.findByCursoIdAndMateriaIdAndAnoLectivo(dto.cursoId(), dto.materiaId(), anoLectivo);
            if (cmOpt.isPresent() && cmOpt.get().getDocente() != null) {
                docenteEfectivoId = cmOpt.get().getDocente().getId();
            }
        }

        // 5. Choque por Docente (mismo día y año lectivo, join Horario -> CursoMateria -> docente_id)
        if (docenteEfectivoId != null) {
            List<Horario> choquesDocente = (horarioIdExcluido != null)
                    ? horarioRepository.buscarSolapamientosDocenteExcluyendoId(docenteEfectivoId, dto.diaSemana(), anoLectivo, dto.horaInicio(), dto.horaFin(), horarioIdExcluido)
                    : horarioRepository.buscarSolapamientosDocente(docenteEfectivoId, dto.diaSemana(), anoLectivo, dto.horaInicio(), dto.horaFin());
            if (!choquesDocente.isEmpty()) {
                Horario c = choquesDocente.get(0);
                throw new IllegalArgumentException("Conflicto de horario: El docente ya tiene clase asignada (" +
                        (c.getMateria() != null ? c.getMateria().getNombre() : "otra asignatura") +
                        ") en el curso " + (c.getCurso() != null ? c.getCurso().getCodigoCurso() : "") +
                        " el " + dto.diaSemana() + " de " + c.getHoraInicio() + " a " + c.getHoraFin());
            }
        }

        // 6. Choque por Salón (mismo espacio físico en el mismo día y año lectivo)
        String salonNombre = (dto.salon() != null && !dto.salon().isBlank()) ? dto.salon().trim() : null;
        if (dto.salonId() != null || salonNombre != null) {
            List<Horario> choquesSalon = (horarioIdExcluido != null)
                    ? horarioRepository.buscarSolapamientosSalonExcluyendoId(dto.salonId(), salonNombre, dto.diaSemana(), anoLectivo, dto.horaInicio(), dto.horaFin(), horarioIdExcluido)
                    : horarioRepository.buscarSolapamientosSalon(dto.salonId(), salonNombre, dto.diaSemana(), anoLectivo, dto.horaInicio(), dto.horaFin());
            if (!choquesSalon.isEmpty()) {
                Horario c = choquesSalon.get(0);
                throw new IllegalArgumentException("Conflicto de salón: El espacio '" +
                        (salonNombre != null ? salonNombre : "físico") + "' ya está ocupado por el curso " +
                        (c.getCurso() != null ? c.getCurso().getCodigoCurso() : "") +
                        " el " + dto.diaSemana() + " de " + c.getHoraInicio() + " a " + c.getHoraFin());
            }
        }

        // 7. Choque por Curso (el mismo grupo de estudiantes no puede tener 2 clases a la vez)
        List<Horario> choquesCurso = (horarioIdExcluido != null)
                ? horarioRepository.buscarSolapamientosCursoExcluyendoId(dto.cursoId(), dto.diaSemana(), dto.horaInicio(), dto.horaFin(), horarioIdExcluido)
                : horarioRepository.buscarSolapamientosCurso(dto.cursoId(), dto.diaSemana(), dto.horaInicio(), dto.horaFin());
        if (!choquesCurso.isEmpty()) {
            Horario c = choquesCurso.get(0);
            throw new IllegalArgumentException("Conflicto de curso: El curso " + curso.getCodigoCurso() +
                    " ya tiene programada la clase de " +
                    (c.getMateria() != null ? c.getMateria().getNombre() : "otra asignatura") +
                    " el " + dto.diaSemana() + " de " + c.getHoraInicio() + " a " + c.getHoraFin());
        }

        // 8. Advertencia (no bloqueante) de Intensidad Horaria Semanal
        List<String> advertencias = new ArrayList<>();
        if (dto.materiaId() != null) {
            materiaRepository.findById(dto.materiaId()).ifPresent(mat -> {
                int intensidadPlanificada = mat.getIntensidadHoraria() != null ? mat.getIntensidadHoraria() : 0;
                if (intensidadPlanificada > 0) {
                    List<Horario> horariosMateria = horarioRepository.findByCursoIdAndMateriaId(dto.cursoId(), dto.materiaId());
                    long minutosTotales = 0;
                    for (Horario h : horariosMateria) {
                        if (horarioIdExcluido == null || !h.getId().equals(horarioIdExcluido)) {
                            minutosTotales += Duration.between(h.getHoraInicio(), h.getHoraFin()).toMinutes();
                        }
                    }
                    minutosTotales += Duration.between(dto.horaInicio(), dto.horaFin()).toMinutes();
                    double horasProgramadas = minutosTotales / 60.0;
                    if (horasProgramadas > intensidadPlanificada) {
                        advertencias.add("Advertencia: Las horas programadas (" + String.format("%.1f", horasProgramadas) +
                                "h) superan la intensidad sugerida para " + mat.getNombre() + " (" + intensidadPlanificada + "h semanales).");
                    }
                }
            });
        }

        return new ValidacionResultado(true, advertencias);
    }

    // Setters auxiliares para tests o reconfiguración dinámica
    public void setConfigMananaInicio(String configMananaInicio) { this.configMananaInicio = configMananaInicio; }
    public void setConfigMananaFin(String configMananaFin) { this.configMananaFin = configMananaFin; }
    public void setConfigTardeInicio(String configTardeInicio) { this.configTardeInicio = configTardeInicio; }
    public void setConfigTardeFin(String configTardeFin) { this.configTardeFin = configTardeFin; }
    public void setConfigToleranciaMinutos(int configToleranciaMinutos) { this.configToleranciaMinutos = configToleranciaMinutos; }
}
