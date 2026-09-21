package com.siga.siga_iea.clases.application;

import com.siga.siga_iea.asistencias.service.AsistenciaService;
import com.siga.siga_iea.calificaciones.entity.Evaluacion;
import com.siga.siga_iea.calificaciones.service.CalificacionesService;
import com.siga.siga_iea.clases.dto.ClaseGestionDetalleDTO;
import com.siga.siga_iea.clases.dto.ClaseTablaNotasDTO;
import com.siga.siga_iea.clases.entity.*;
import com.siga.siga_iea.clases.service.ClaseService;
import com.siga.siga_iea.usuarios.entity.Docente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Servicio de Aplicación (Capa de Casos de Uso) para la Gestión de Clases.
 * Extrae y desacopla la orquestación de negocio pesada que anteriormente
 * residía dentro de ClaseController.
 */
@Service
public class ClaseGestionAppService {

    private final ClaseService claseService;
    private final CalificacionesService calificacionesService;
    private final AsistenciaService asistenciaService;

    public ClaseGestionAppService(ClaseService claseService,
                                  CalificacionesService calificacionesService,
                                  AsistenciaService asistenciaService) {
        this.claseService = claseService;
        this.calificacionesService = calificacionesService;
        this.asistenciaService = asistenciaService;
    }

    public static String obtenerNombreDiaEspanol(DayOfWeek dow) {
        if (dow == null) return "Lunes";
        return switch (dow) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    @Transactional
    public ClaseGestionDetalleDTO prepararGestionDetalle(String codigo, Optional<Docente> docenteLogueadoOpt, boolean esAdmin) {
        Optional<Clase> claseOpt = claseService.buscarPorCodigo(codigo, "2026");
        Clase c;
        if (claseOpt.isPresent()) {
            c = claseOpt.get();
        } else {
            String degree = (codigo != null && codigo.contains("-")) ? codigo.split("-")[0] + "°" : (codigo != null ? codigo : "11°");
            String group = (codigo != null && codigo.contains("-")) ? codigo.split("-")[1] : "01";
            c = claseService.crearCurso(degree, group, "Mañana", 35, null, "2026");
        }

        ClaseGestionDetalleDTO detalleDTO = new ClaseGestionDetalleDTO();
        detalleDTO.setCursoId(c.getId().toString());
        detalleDTO.setCodigoCurso(c.getCodigoCurso());
        detalleDTO.setGradoCurso(c.getGrado());
        detalleDTO.setDirectorCurso(c.getDirector() != null ? c.getDirector().getNombreCompleto() : "Sin asignar");
        detalleDTO.setJornadaCurso(c.getJornada());
        detalleDTO.setEsAdmin(esAdmin);

        List<CursoEstudiante> estudiantesCE = claseService.listarEstudiantesDeCurso(c.getId());
        List<Map<String, Object>> estudiantesData = new ArrayList<>();
        int idx = 1;
        for (CursoEstudiante ce : estudiantesCE) {
            if (ce.getEstudiante() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ce.getEstudiante().getId().toString());
                map.put("numIdx", idx++);
                map.put("nombre", ce.getEstudiante().getNombreCompleto());
                map.put("documento", ce.getEstudiante().getNumeroDocumento());
                map.put("asistencia", "Presente");
                estudiantesData.add(map);
            }
        }
        detalleDTO.setEstudiantesData(estudiantesData);

        List<Horario> horarios = claseService.listarHorariosDeCurso(c.getId());
        List<Map<String, String>> horariosData = new ArrayList<>();
        for (Horario h : horarios) {
            Map<String, String> hm = new HashMap<>();
            hm.put("dia", h.getDiaSemana());
            hm.put("materia", h.getMateria() != null ? h.getMateria().getNombre() : "");
            hm.put("docente", h.getDocente() != null ? h.getDocente().getNombreCompleto() : "Sin docente asignado");
            hm.put("hora", (h.getHoraInicio() != null ? h.getHoraInicio().toString() : "07:00") + " - " + (h.getHoraFin() != null ? h.getHoraFin().toString() : "08:30"));
            horariosData.add(hm);
        }
        detalleDTO.setHorariosData(horariosData);

        LocalDate hoy = LocalDate.now();
        ClaseTablaNotasDTO tablaDTO = prepararTablaNotas(c.getId(), null, null, 1, hoy, docenteLogueadoOpt, esAdmin);
        detalleDTO.setTablaNotasDTO(tablaDTO);
        detalleDTO.setHorarioBannerTexto(tablaDTO.getHorarioBannerTexto());

        return detalleDTO;
    }

    @Transactional
    public ClaseTablaNotasDTO prepararTablaNotas(UUID cursoId,
                                                String materiaNombre,
                                                UUID materiaId,
                                                Integer periodo,
                                                LocalDate fecha,
                                                Optional<Docente> docenteLogueadoOpt,
                                                boolean esAdmin) {
        if (fecha == null) fecha = LocalDate.now();
        if (periodo == null) periodo = 1;

        ClaseTablaNotasDTO dto = new ClaseTablaNotasDTO();
        dto.setCursoId(cursoId);
        dto.setPeriodo(periodo);
        dto.setFecha(fecha);

        String diaSemana = obtenerNombreDiaEspanol(fecha.getDayOfWeek());
        dto.setDiaSemana(diaSemana);

        List<Horario> horarios = claseService.listarHorariosDeCurso(cursoId);
        List<Horario> horariosDelDia = horarios.stream()
                .filter(h -> h.getDiaSemana() != null && h.getDiaSemana().equalsIgnoreCase(diaSemana))
                .toList();

        List<CursoEstudiante> estudiantesCE = claseService.listarEstudiantesDeCurso(cursoId);
        dto.setEstudiantesCE(estudiantesCE);

        if (horariosDelDia.isEmpty() || (materiaNombre != null && materiaNombre.isBlank() && materiaId == null)) {
            dto.setTieneHorarioHoy(false);
            dto.setMateriaNombre("");
            dto.setMateriaId(null);
            dto.setCursoMateriaId(null);
            dto.setHorarioBannerTexto(esAdmin
                    ? "No hay clases programadas en el horario para este día."
                    : "No tienes clases programadas en este curso para este día.");
            return dto;
        }

        final UUID targetMateriaId = materiaId;
        final String targetMateriaNombre = materiaNombre;
        Optional<Horario> horarioMatch = Optional.empty();
        if (targetMateriaId != null) {
            horarioMatch = horariosDelDia.stream()
                    .filter(h -> h.getMateria() != null && h.getMateria().getId().equals(targetMateriaId))
                    .findFirst();
        } else if (targetMateriaNombre != null && !targetMateriaNombre.isBlank()) {
            horarioMatch = horariosDelDia.stream()
                    .filter(h -> h.getMateria() != null && h.getMateria().getNombre().equalsIgnoreCase(targetMateriaNombre))
                    .findFirst();
        }

        Horario hSeleccionado = null;
        if (!esAdmin && docenteLogueadoOpt.isPresent()) {
            Docente miDocente = docenteLogueadoOpt.get();
            hSeleccionado = horariosDelDia.stream()
                    .filter(h -> h.getDocente() != null && (
                            h.getDocente().getId().equals(miDocente.getId()) ||
                            (h.getDocente().getNumeroDocumento() != null && h.getDocente().getNumeroDocumento().equalsIgnoreCase(miDocente.getNumeroDocumento()))
                    ))
                    .findFirst()
                    .orElse(null);
        } else {
            hSeleccionado = horarioMatch.orElse(!horariosDelDia.isEmpty() ? horariosDelDia.get(0) : null);
        }

        if (hSeleccionado == null) {
            dto.setTieneHorarioHoy(false);
            dto.setMateriaNombre("");
            dto.setMateriaId(null);
            dto.setCursoMateriaId(null);
            dto.setHorarioBannerTexto(esAdmin
                    ? "No hay clases programadas en el horario para este día."
                    : "No tienes clases programadas en este curso para este día.");
            return dto;
        }

        String selectedMateriaNombre = hSeleccionado.getMateria() != null ? hSeleccionado.getMateria().getNombre() : "";
        UUID selectedMateriaId = hSeleccionado.getMateria() != null ? hSeleccionado.getMateria().getId() : null;

        if (selectedMateriaId == null && selectedMateriaNombre.isBlank()) {
            dto.setTieneHorarioHoy(false);
            dto.setMateriaNombre("");
            dto.setMateriaId(null);
            dto.setCursoMateriaId(null);
            dto.setHorarioBannerTexto("No hay clases programadas en el horario para este día.");
            return dto;
        }

        dto.setTieneHorarioHoy(true);
        String horaStr = (hSeleccionado.getHoraInicio() != null ? hSeleccionado.getHoraInicio().toString() : "07:00") + " - " + (hSeleccionado.getHoraFin() != null ? hSeleccionado.getHoraFin().toString() : "08:30");
        String docStr = (hSeleccionado.getDocente() != null) ? " • Docente: " + hSeleccionado.getDocente().getNombreCompleto() : "";
        dto.setHorarioBannerTexto("Horario: " + horaStr + docStr);

        CursoMateria cm = (selectedMateriaId != null)
                ? calificacionesService.obtenerOCrearCursoMateria(cursoId, selectedMateriaId, "2026")
                : calificacionesService.obtenerOCrearCursoMateriaPorNombre(cursoId, selectedMateriaNombre, "2026");

        List<Evaluacion> evaluaciones = calificacionesService.obtenerEvaluacionesPorCursoYMateria(cm.getId(), periodo);
        BigDecimal sumaPesos = evaluaciones.stream()
                .map(e -> e.getPeso() != null ? e.getPeso() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, Map<UUID, BigDecimal>> calificacionesMapa = calificacionesService.obtenerCalificacionesMapa(cm.getId(), periodo);
        Map<UUID, String> asistenciasMapa = asistenciaService.obtenerMapaEstadosAsistencia(cursoId, fecha, cm.getMateria().getId());

        Map<UUID, BigDecimal> notasFinales = new HashMap<>();
        for (CursoEstudiante ce : estudiantesCE) {
            if (ce.getEstudiante() != null) {
                UUID estId = ce.getEstudiante().getId();
                BigDecimal notaFinal = calificacionesService.calcularNotaFinalPeriodo(estId, cm.getId(), periodo);
                notasFinales.put(estId, notaFinal);
            }
        }

        dto.setCursoMateriaId(cm.getId());
        dto.setMateriaId(cm.getMateria().getId());
        dto.setMateriaNombre(cm.getMateria().getNombre());
        dto.setEvaluaciones(evaluaciones);
        dto.setSumaPesos(sumaPesos);
        dto.setCalificacionesMapa(calificacionesMapa);
        dto.setAsistenciasMapa(asistenciasMapa);
        dto.setNotasFinales(notasFinales);

        return dto;
    }

    @Transactional
    public int procesarGuardadoHorarioGrid(UUID cursoId, Map<String, String> allParams) {
        claseService.limpiarHorarioCurso(cursoId);

        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        int guardados = 0;

        Set<Integer> slotIndices = new TreeSet<>();
        for (String key : allParams.keySet()) {
            if (key.startsWith("slot_") && key.endsWith("_inicio")) {
                try {
                    String idxStr = key.substring(5, key.indexOf("_inicio"));
                    slotIndices.add(Integer.parseInt(idxStr));
                } catch (Exception ignored) {}
            }
        }

        for (Integer i : slotIndices) {
            String horaInicio = allParams.get("slot_" + i + "_inicio");
            String horaFin = allParams.get("slot_" + i + "_fin");
            String salon = allParams.getOrDefault("slot_" + i + "_salon", "Aula 101");

            if (horaInicio == null || horaFin == null || horaInicio.isBlank() || horaFin.isBlank()) continue;

            for (String dia : dias) {
                String matKey = "slot_" + i + "_" + dia + "_materiaId";
                String docKey = "slot_" + i + "_" + dia + "_docenteId";

                String matIdStr = allParams.get(matKey);
                String docIdStr = allParams.get(docKey);

                if (matIdStr == null || matIdStr.isBlank()) {
                    String diaAlt = dia.contains("é") ? dia.replace("é", "e") : dia.replace("e", "é");
                    String matKeyAlt = "slot_" + i + "_" + diaAlt + "_materiaId";
                    String docKeyAlt = "slot_" + i + "_" + diaAlt + "_docenteId";
                    if (allParams.containsKey(matKeyAlt)) {
                        matIdStr = allParams.get(matKeyAlt);
                        docIdStr = allParams.get(docKeyAlt);
                    }
                }

                if (matIdStr != null && !matIdStr.isBlank()) {
                    UUID materiaId = UUID.fromString(matIdStr);
                    UUID docenteId = (docIdStr != null && !docIdStr.isBlank()) ? UUID.fromString(docIdStr) : null;

                    claseService.guardarHorarioBloque(cursoId, dia, materiaId, docenteId, horaInicio, horaFin, salon);
                    guardados++;
                }
            }
        }
        return guardados;
    }
}
