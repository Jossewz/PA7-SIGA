package com.siga.siga_iea.asistencias.service;

import com.siga.siga_iea.asistencias.dto.AsistenciaItemDto;
import com.siga.siga_iea.asistencias.entity.Asistencia;
import com.siga.siga_iea.asistencias.entity.SesionClase;
import com.siga.siga_iea.asistencias.repository.AsistenciaRepository;
import com.siga.siga_iea.asistencias.repository.SesionClaseRepository;
import com.siga.siga_iea.auth.enums.RolEnum;
import com.siga.siga_iea.auth.service.CurrentUserContextService;
import com.siga.siga_iea.clases.application.CursoGestionAppService;
import com.siga.siga_iea.clases.dto.HorarioDto;
import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.CursoMateria;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.clases.repository.CursoEstudianteRepository;
import com.siga.siga_iea.clases.repository.CursoMateriaRepository;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.clases.repository.HorarioRepository;
import com.siga.siga_iea.clases.validation.HorarioValidator;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AsistenciaService {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaService.class);

    private final SesionClaseRepository sesionClaseRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final CursoRepository cursoRepository;
    private final HorarioRepository horarioRepository;
    private final CursoMateriaRepository cursoMateriaRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;
    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;
    private final HorarioValidator horarioValidator;
    private final CurrentUserContextService currentUserContextService;
    private final Clock clock;

    @Autowired
    @Lazy
    private AsistenciaService self;

    @Autowired
    public AsistenciaService(SesionClaseRepository sesionClaseRepository,
                             AsistenciaRepository asistenciaRepository,
                             CursoRepository cursoRepository,
                             HorarioRepository horarioRepository,
                             CursoMateriaRepository cursoMateriaRepository,
                             CursoEstudianteRepository cursoEstudianteRepository,
                             EstudianteRepository estudianteRepository,
                             DocenteRepository docenteRepository,
                             HorarioValidator horarioValidator,
                             CurrentUserContextService currentUserContextService,
                             Clock clock) {
        this.sesionClaseRepository = sesionClaseRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.cursoRepository = cursoRepository;
        this.horarioRepository = horarioRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.cursoEstudianteRepository = cursoEstudianteRepository;
        this.estudianteRepository = estudianteRepository;
        this.docenteRepository = docenteRepository;
        this.horarioValidator = horarioValidator;
        this.currentUserContextService = currentUserContextService;
        this.clock = clock != null ? clock : Clock.system(ZoneId.of("America/Bogota"));
    }

    public AsistenciaService(SesionClaseRepository sesionClaseRepository,
                             AsistenciaRepository asistenciaRepository,
                             CursoRepository cursoRepository,
                             HorarioRepository horarioRepository,
                             CursoMateriaRepository cursoMateriaRepository,
                             CursoEstudianteRepository cursoEstudianteRepository,
                             EstudianteRepository estudianteRepository,
                             DocenteRepository docenteRepository,
                             HorarioValidator horarioValidator,
                             CurrentUserContextService currentUserContextService) {
        this(sesionClaseRepository, asistenciaRepository, cursoRepository, horarioRepository,
             cursoMateriaRepository, cursoEstudianteRepository, estudianteRepository,
             docenteRepository, horarioValidator, currentUserContextService,
             Clock.system(ZoneId.of("America/Bogota")));
    }

    /**
     * Punto de entrada para abrir día.
     * Basado puramente en la restricción de base de datos uk_sesion_curso_fecha_hora.
     * Si ocurre DataIntegrityViolationException por colisión concurrente multi-hilo o multi-nodo,
     * la transacción interna (REQUIRES_NEW) se descarta limpiamente y se reconsultan las sesiones
     * en una transacción nueva sin contaminación de rollback-only.
     */
    public List<SesionClase> abrirDia(UUID cursoId, LocalDate fecha) {
        if (cursoId == null || fecha == null) {
            throw new IllegalArgumentException("cursoId y fecha son obligatorios para abrir el día");
        }
        try {
            AsistenciaService service = (self != null) ? self : this;
            return service.abrirDiaInterno(cursoId, fecha);
        } catch (DataIntegrityViolationException ex) {
            log.info("Colisión concurrente detectada en apertura de día para curso {} en fecha {}. Reconsultando sesiones creadas.",
                    cursoId, fecha);
            AsistenciaService service = (self != null) ? self : this;
            return service.consultarSesiones(cursoId, fecha);
        }
    }

    @Transactional(readOnly = true)
    public List<SesionClase> consultarSesiones(UUID cursoId, LocalDate fecha) {
        return sesionClaseRepository.findByCursoIdAndFechaOrderByHoraInicioAsc(cursoId, fecha);
    }

    /**
     * Lógica transaccional de apertura de día:
     * - Primaria (Transición a 5°): crea una sola sesión tipo 'JORNADA' con jornada completa y sin materia.
     * - Secundaria / Media: genera las sesiones a partir de los Horarios del día, copiando el docente
     *   estrictamente de CursoMateria.
     */
    @Transactional
    public List<SesionClase> abrirDiaInterno(UUID cursoId, LocalDate fecha) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));

        List<SesionClase> existentes = sesionClaseRepository.findByCursoIdAndFechaOrderByHoraInicioAsc(cursoId, fecha);

        if (esPrimariaOPreescolar(curso.getGrado())) {
            if (!existentes.isEmpty()) {
                return existentes;
            }

            LocalTime horaInicio = LocalTime.of(7, 0);
            LocalTime horaFin = LocalTime.of(12, 0);
            if (curso.getJornada() != null && curso.getJornada().equalsIgnoreCase("Tarde")) {
                horaInicio = LocalTime.of(13, 0);
                horaFin = LocalTime.of(18, 0);
            }

            Docente docenteDirector = curso.getDirector();
            SesionClase sesionJornada = new SesionClase(
                    curso,
                    null,
                    null,
                    docenteDirector,
                    fecha,
                    horaInicio,
                    horaFin,
                    "JORNADA"
            );
            sesionJornada.setEstado("DICTADA");
            sesionJornada = sesionClaseRepository.save(sesionJornada);
            log.info("Sesión JORNADA abierta para curso {} en fecha {}", curso.getGrado(), fecha);
            return List.of(sesionJornada);
        }

        // Secundaria / Media
        String diaSemanaEspanol = CursoGestionAppService.obtenerNombreDiaEspanol(fecha.getDayOfWeek());
        String diaSemanaNorm = normalizarTexto(diaSemanaEspanol);

        List<Horario> horarios = horarioRepository.findByCursoId(cursoId).stream()
                .filter(h -> h.getDiaSemana() != null && normalizarTexto(h.getDiaSemana()).equalsIgnoreCase(diaSemanaNorm))
                .sorted(Comparator.comparing(Horario::getHoraInicio))
                .toList();

        List<SesionClase> resultado = new ArrayList<>(existentes);
        Set<LocalTime> horasExistentes = new HashSet<>();
        for (SesionClase s : existentes) {
            horasExistentes.add(s.getHoraInicio());
        }

        for (Horario h : horarios) {
            if (horasExistentes.contains(h.getHoraInicio())) {
                continue;
            }

            // Docente sale estrictamente de CursoMateria
            UUID materiaId = h.getMateria() != null ? h.getMateria().getId() : null;
            CursoMateria cm = null;
            Docente docenteEfectivo = null;

            if (materiaId != null) {
                cm = cursoMateriaRepository.findByCursoIdAndMateriaIdAndAnoLectivo(cursoId, materiaId, curso.getAnoLectivo())
                        .orElse(null);
                if (cm != null) {
                    docenteEfectivo = cm.getDocente();
                }
            }

            SesionClase nuevaSesion = new SesionClase(
                    curso,
                    cm,
                    h,
                    docenteEfectivo,
                    fecha,
                    h.getHoraInicio(),
                    h.getHoraFin(),
                    "ASIGNATURA"
            );
            nuevaSesion.setEstado("DICTADA");
            nuevaSesion = sesionClaseRepository.save(nuevaSesion);
            resultado.add(nuevaSesion);
            horasExistentes.add(nuevaSesion.getHoraInicio());
        }

        resultado.sort(Comparator.comparing(SesionClase::getHoraInicio));
        return resultado;
    }

    /**
     * Cancela una sesión de clase. Una sesión cancelada rechaza cualquier registro de asistencia posterior.
     */
    @Transactional
    public SesionClase cancelarSesion(UUID sesionId, String motivo) {
        SesionClase sesion = sesionClaseRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sesionId));

        sesion.setEstado("CANCELADA");
        if (motivo != null && !motivo.isBlank()) {
            sesion.setTema((sesion.getTema() != null ? sesion.getTema() + " | " : "") + "Cancelada: " + motivo.trim());
        }
        return sesionClaseRepository.save(sesion);
    }

    /**
     * Asigna un docente de reemplazo a una sesión de clase, cambiando su estado a 'REEMPLAZO'.
     */
    @Transactional
    public SesionClase asignarReemplazo(UUID sesionId, UUID docenteReemplazoId, String motivo) {
        SesionClase sesion = sesionClaseRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sesionId));

        if ("CANCELADA".equalsIgnoreCase(sesion.getEstado())) {
            throw new IllegalStateException("No se puede asignar reemplazo a una sesión cancelada (ID: " + sesionId + ")");
        }

        Docente reemplazo = docenteRepository.findById(docenteReemplazoId)
                .orElseThrow(() -> new IllegalArgumentException("Docente de reemplazo no encontrado: " + docenteReemplazoId));

        sesion.setDocente(reemplazo);
        sesion.setEstado("REEMPLAZO");
        if (motivo != null && !motivo.isBlank()) {
            sesion.setTema((sesion.getTema() != null ? sesion.getTema() + " | " : "") + "Reemplazo: " + motivo.trim());
        }
        return sesionClaseRepository.save(sesion);
    }

    /**
     * Registra asistencia usando el contexto de seguridad autenticado (Docente asignado, Admin o Coordinación).
     */
    @Transactional
    public List<Asistencia> registrar(UUID sesionId, List<AsistenciaItemDto> items) {
        Docente docente = null;
        RolEnum rol = null;
        if (currentUserContextService != null && currentUserContextService.getAuthentication().isPresent()) {
            docente = currentUserContextService.getDocenteAutenticado().orElse(null);
            rol = currentUserContextService.getRolAutenticado();
        }
        return registrar(sesionId, items, docente, rol);
    }

    /**
     * Registra asistencia con validación explícita de operador:
     * - Rechaza si la sesión está en estado CANCELADA.
     * - Autorización: Solo el docente efectivo asignado a la sesión o personal administrativo/coordinación/admin.
     * - Valida que cada estudiante esté inscrito en el curso (CursoEstudiante).
     * - Realiza upsert sobre uk_asistencia_sesion_estudiante.
     */
    public List<Asistencia> registrar(UUID sesionId, List<AsistenciaItemDto> items, Docente docenteOperador, RolEnum rolOperador) {
        if (sesionId == null) {
            throw new IllegalArgumentException("sesionId es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            AsistenciaService service = (self != null) ? self : this;
            return service.registrarInterno(sesionId, items, docenteOperador, rolOperador);
        } catch (DataIntegrityViolationException ex) {
            log.info("Colisión concurrente detectada en persistencia de asistencias para sesión {}. Reintentando actualización.", sesionId);
            AsistenciaService service = (self != null) ? self : this;
            return service.registrarInterno(sesionId, items, docenteOperador, rolOperador);
        }
    }

    @Transactional
    public List<Asistencia> registrarInterno(UUID sesionId, List<AsistenciaItemDto> items, Docente docenteOperador, RolEnum rolOperador) {
        SesionClase sesion = sesionClaseRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sesionId));

        // 1. Validar estado de la sesión
        if ("CANCELADA".equalsIgnoreCase(sesion.getEstado())) {
            throw new IllegalStateException("No se puede registrar asistencia en una sesión cancelada (ID: " + sesionId + ")");
        }

        // 2. Validar autorización del operador (si se proporciona contexto o docente operador)
        validarAutorizacionRegistro(sesion, docenteOperador, rolOperador);

        // 3. Validar estudiantes e insertar/actualizar (upsert)
        UUID cursoId = sesion.getCurso().getId();
        List<Asistencia> guardadas = new ArrayList<>();

        for (AsistenciaItemDto item : items) {
            if (item.estudianteId() == null) continue;

            boolean matriculado = cursoEstudianteRepository.existsByCursoIdAndEstudianteId(cursoId, item.estudianteId());
            if (!matriculado) {
                throw new IllegalArgumentException("El estudiante " + item.estudianteId() +
                        " no está inscrito en el curso asignado a la sesión (" + cursoId + ")");
            }

            Optional<Asistencia> opt = asistenciaRepository.findBySesionIdAndEstudianteId(sesionId, item.estudianteId());
            Asistencia asistencia;
            String estadoNormalizado = normalizarEstado(item.estado());

            if (opt.isPresent()) {
                asistencia = opt.get();
                asistencia.setEstado(estadoNormalizado);
                if (item.observaciones() != null) {
                    asistencia.setObservaciones(item.observaciones());
                }
            } else {
                Estudiante estudiante = estudianteRepository.findById(item.estudianteId())
                        .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + item.estudianteId()));
                asistencia = new Asistencia(sesion, estudiante, estadoNormalizado, item.observaciones());
            }

            guardadas.add(asistenciaRepository.saveAndFlush(asistencia));
        }

        return guardadas;
    }

    /**
     * Valida que el operador tenga permiso para consultar/ver una sesión de clase:
     * - Admin y Personal Administrativo tienen acceso a cualquier sesión.
     * - El docente únicamente puede acceder a sus propias sesiones.
     */
    public void autorizarAccesoSesion(SesionClase sesion, Docente docenteOperador, RolEnum rolOperador) {
        if (sesion == null) {
            throw new IllegalArgumentException("Sesión no encontrada");
        }
        if (rolOperador != null && rolOperador.esAdminOAdministrativo()) {
            return;
        }
        if (docenteOperador == null) {
            throw new SecurityException("Acceso denegado: Se requiere docente autenticado o rol administrativo.");
        }
        Docente docenteSesion = sesion.getDocente();
        if (docenteSesion == null || !docenteSesion.getId().equals(docenteOperador.getId())) {
            throw new SecurityException("Acceso denegado: No tienes permiso sobre esta sesión de clase ajena.");
        }
    }

    private void validarAutorizacionRegistro(SesionClase sesion, Docente docenteOperador, RolEnum rolOperador) {
        // Si no se pasaron datos de operador ni hay contexto de seguridad activo (ej. pruebas sin auth), se permite
        if (docenteOperador == null && rolOperador == null) {
            return;
        }

        // Admin o Coordinación/Personal Administrativo tienen acceso pleno (incluso fechas anteriores)
        if (rolOperador != null && rolOperador.esAdminOAdministrativo()) {
            return;
        }

        // Ventana de edición: Docentes solo pueden registrar o modificar el día actual
        LocalDate hoy = LocalDate.now(clock);
        if (!sesion.getFecha().equals(hoy)) {
            throw new SecurityException("Ventana de edición cerrada: Los docentes solo pueden registrar o modificar asistencia para la fecha de hoy (" + hoy + ").");
        }

        // Docente asignado a la sesión (sea titular o reemplazo)
        if (docenteOperador != null) {
            Docente docenteSesion = sesion.getDocente();
            if (docenteSesion != null && docenteSesion.getId().equals(docenteOperador.getId())) {
                return;
            }
            throw new SecurityException("Acceso denegado: El docente " + docenteOperador.getNombres() + " " +
                    docenteOperador.getApellidos() + " no está asignado a esta sesión de clase.");
        }

        throw new SecurityException("Acceso denegado: Se requiere rol de Docente asignado a la sesión o Coordinación/Administración.");
    }

    /**
     * Creación de sesiones extraordinarias con validación de choques mediante HorarioValidator
     * y verificación de solapamientos con sesiones de clase (SesionClase) ya existentes en el día.
     */
    @Transactional
    public SesionClase crearSesionExtraordinaria(UUID cursoId, UUID materiaId, UUID docenteId,
                                                 LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                                 String tema) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));

        String diaSemana = CursoGestionAppService.obtenerNombreDiaEspanol(fecha.getDayOfWeek());
        HorarioDto horarioDto = new HorarioDto(cursoId, diaSemana, horaInicio, horaFin, materiaId, docenteId, null, null);

        // 1. Valida contra los Horarios semanales recurrentes y límites de jornada
        horarioValidator.validar(horarioDto, null);

        // 2. Valida contra SesionClase ya existentes en la fecha para este curso (evita solapamientos parciales o totales)
        List<SesionClase> sesionesCurso = sesionClaseRepository.findByCursoIdAndFechaOrderByHoraInicioAsc(cursoId, fecha);
        for (SesionClase sc : sesionesCurso) {
            if ("CANCELADA".equalsIgnoreCase(sc.getEstado())) {
                continue;
            }
            if (horaInicio.isBefore(sc.getHoraFin()) && horaFin.isAfter(sc.getHoraInicio())) {
                throw new IllegalStateException(String.format(
                        "Choque de horario: El curso ya tiene una sesión programada entre %s y %s en la fecha %s.",
                        sc.getHoraInicio(), sc.getHoraFin(), fecha
                ));
            }
        }

        CursoMateria cm = null;
        if (materiaId != null) {
            cm = cursoMateriaRepository.findByCursoIdAndMateriaIdAndAnoLectivo(cursoId, materiaId, curso.getAnoLectivo())
                    .orElse(null);
        }

        Docente docente = null;
        if (docenteId != null) {
            docente = docenteRepository.findById(docenteId).orElse(null);
        } else if (cm != null) {
            docente = cm.getDocente();
        }

        // 3. Valida contra SesionClase ya existentes en la fecha para el docente asignado (en cualquier curso)
        if (docente != null) {
            List<SesionClase> sesionesDocente = sesionClaseRepository.findByDocenteIdAndFechaOrderByHoraInicioAsc(docente.getId(), fecha);
            for (SesionClase sd : sesionesDocente) {
                if ("CANCELADA".equalsIgnoreCase(sd.getEstado())) {
                    continue;
                }
                if (horaInicio.isBefore(sd.getHoraFin()) && horaFin.isAfter(sd.getHoraInicio())) {
                    throw new IllegalStateException(String.format(
                            "Choque de horario: El docente %s %s ya tiene una sesión programada entre %s y %s en la fecha %s.",
                            docente.getNombres(), docente.getApellidos(),
                            sd.getHoraInicio(), sd.getHoraFin(), fecha
                    ));
                }
            }
        }

        SesionClase extraordinaria = new SesionClase(
                curso,
                cm,
                null,
                docente,
                fecha,
                horaInicio,
                horaFin,
                "ASIGNATURA"
        );
        extraordinaria.setTema(tema);
        extraordinaria.setEstado("DICTADA");
        return sesionClaseRepository.save(extraordinaria);
    }

    /**
     * Soporte para el cuadro de calificaciones y UI de gestión de curso.
     * Retorna un mapa estudianteId -> estado de asistencia para la materia y fecha dada.
     */
    @Transactional
    public Map<UUID, String> obtenerMapaEstadosAsistencia(UUID cursoId, LocalDate fecha, UUID materiaId) {
        List<SesionClase> sesiones = abrirDia(cursoId, fecha);
        SesionClase sesionObjetivo = null;

        for (SesionClase s : sesiones) {
            if (materiaId == null || (s.getCursoMateria() != null && s.getCursoMateria().getMateria().getId().equals(materiaId))) {
                sesionObjetivo = s;
                break;
            }
        }

        Map<UUID, String> mapa = new HashMap<>();
        if (sesionObjetivo != null) {
            List<Asistencia> asistencias = asistenciaRepository.findBySesionId(sesionObjetivo.getId());
            for (Asistencia a : asistencias) {
                if (a.getEstudiante() != null) {
                    mapa.put(a.getEstudiante().getId(), a.getEstado());
                }
            }
        }
        return mapa;
    }

    /**
     * Alterna cíclicamente el estado de asistencia de un estudiante:
     * PRESENTE -> AUSENTE -> EXCUSADO -> PRESENTE.
     * Pasa obligatoriamente por registrar(...) asegurando la regla de "una sola puerta".
     */
    @Transactional
    public Asistencia toggleAsistencia(UUID cursoId, UUID estudianteId, LocalDate fecha, UUID materiaId) {
        List<SesionClase> sesiones = abrirDia(cursoId, fecha);
        SesionClase sesion = null;
        for (SesionClase s : sesiones) {
            if (materiaId == null || (s.getCursoMateria() != null && s.getCursoMateria().getMateria().getId().equals(materiaId))) {
                sesion = s;
                break;
            }
        }

        if (sesion == null && !sesiones.isEmpty()) {
            sesion = sesiones.get(0);
        }

        if (sesion == null) {
            throw new IllegalStateException("No hay sesión de clase disponible para registrar asistencia");
        }

        Optional<Asistencia> opt = asistenciaRepository.findBySesionIdAndEstudianteId(sesion.getId(), estudianteId);
        String nuevoEstado;
        String observaciones = null;

        if (opt.isPresent()) {
            String actual = opt.get().getEstado();
            if ("PRESENTE".equalsIgnoreCase(actual)) {
                nuevoEstado = "AUSENTE";
            } else if ("AUSENTE".equalsIgnoreCase(actual)) {
                nuevoEstado = "EXCUSADO";
            } else {
                nuevoEstado = "PRESENTE";
            }
        } else {
            nuevoEstado = "AUSENTE";
        }

        // Pasa estrictamente por registrar() -> "una sola puerta"
        List<Asistencia> res = registrar(sesion.getId(), List.of(new AsistenciaItemDto(estudianteId, nuevoEstado, observaciones)));
        return res.isEmpty() ? null : res.get(0);
    }

    public static boolean esPrimariaOPreescolar(String grado) {
        if (grado == null) return false;
        String clean = grado.trim().toLowerCase();
        if (clean.contains("trans") || clean.contains("jard") || clean.contains("pre")) {
            return true;
        }
        String num = grado.replaceAll("[^0-9]", "");
        if (!num.isEmpty()) {
            try {
                int g = Integer.parseInt(num);
                return g <= 5;
            } catch (NumberFormatException ignored) {}
        }
        return false;
    }

    private String normalizarTexto(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim();
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) return "PRESENTE";
        String e = estado.trim().toUpperCase();
        if (e.equals("NO PRESENTE") || e.equals("NO_PRESENTE") || e.equals("AUSENTE")) {
            return "AUSENTE";
        }
        if (e.equals("EXCUSADO") || e.equals("EXCUSA")) {
            return "EXCUSADO";
        }
        if (e.equals("TARDE") || e.equals("RETARDO")) {
            return "TARDE";
        }
        return "PRESENTE";
    }
}
