package com.siga.siga_iea.asistencias.service;

import com.siga.siga_iea.asistencias.entity.SesionClase;
import com.siga.siga_iea.asistencias.repository.SesionClaseRepository;
import com.siga.siga_iea.clases.application.CursoGestionAppService;
import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.clases.repository.HorarioRepository;
import com.siga.siga_iea.usuarios.entity.Docente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Servicio de orquestación de la jornada diaria del docente ("Mi Jornada").
 * Deduce en backend los cursos y sesiones del docente (direcciones de grupo y horarios del día)
 * sin que el cliente suministre cursoId ni fecha.
 */
@Service
public class MiJornadaService {

    private final Clock clock;
    private final CursoRepository cursoRepo;
    private final HorarioRepository horarioRepo;
    private final SesionClaseRepository sesionRepo;
    private final AsistenciaService asistenciaService;

    public MiJornadaService(Clock clock,
                            CursoRepository cursoRepo,
                            HorarioRepository horarioRepo,
                            SesionClaseRepository sesionRepo,
                            AsistenciaService asistenciaService) {
        this.clock = clock;
        this.cursoRepo = cursoRepo;
        this.horarioRepo = horarioRepo;
        this.sesionRepo = sesionRepo;
        this.asistenciaService = asistenciaService;
    }

    public record JornadaVista(
            LocalDate fecha,
            boolean diaHabil,
            boolean tieneClases,
            List<SesionClase> sesiones
    ) {
        public LocalDate getFecha() { return fecha; }
        public boolean isDiaHabil() { return diaHabil; }
        public boolean isTieneClases() { return tieneClases; }
        public List<SesionClase> getSesiones() { return sesiones; }
    }

    @Transactional(readOnly = true)
    public JornadaVista vistaHoy(Docente docente) {
        LocalDate hoy = LocalDate.now(clock);
        if (esFinDeSemana(hoy)) {
            return new JornadaVista(hoy, false, false, List.of());
        }
        boolean tieneClases = !cursosDelDia(docente, hoy).isEmpty();
        var sesiones = sesionRepo.findByDocenteIdAndFechaOrderByHoraInicioAsc(docente.getId(), hoy);
        return new JornadaVista(hoy, true, tieneClases, sesiones);
    }

    // SIN @Transactional: abrirDia maneja su propia transacción y el reintento por colisión concurrente
    public JornadaVista iniciar(Docente docente) {
        LocalDate hoy = LocalDate.now(clock);
        if (!esFinDeSemana(hoy)) {
            cursosDelDia(docente, hoy).forEach(c -> asistenciaService.abrirDia(c.getId(), hoy));
        }
        return vistaHoy(docente);
    }

    private Collection<Curso> cursosDelDia(Docente d, LocalDate hoy) {
        if (d == null || d.getId() == null) {
            return Collections.emptyList();
        }
        Map<UUID, Curso> cursos = new LinkedHashMap<>();
        cursoRepo.findByDirectorId(d.getId()).forEach(c -> cursos.put(c.getId(), c));
        horarioRepo.findCursosDocentePorDia(d.getId(), nombreDia(hoy))
                   .forEach(c -> cursos.put(c.getId(), c));
        return cursos.values();
    }

    public static boolean esFinDeSemana(LocalDate f) {
        return f.getDayOfWeek() == DayOfWeek.SATURDAY || f.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public static String nombreDia(LocalDate f) {
        return switch (f.getDayOfWeek()) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
}
