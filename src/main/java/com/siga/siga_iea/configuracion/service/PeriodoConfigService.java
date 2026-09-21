package com.siga.siga_iea.configuracion.service;

import com.siga.siga_iea.configuracion.entity.ConfiguracionInstitucional;
import com.siga.siga_iea.configuracion.entity.PeriodoAcademico;
import com.siga.siga_iea.configuracion.repository.ConfiguracionInstitucionalRepository;
import com.siga.siga_iea.configuracion.repository.PeriodoAcademicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PeriodoConfigService {

    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final ConfiguracionInstitucionalRepository configuracionInstitucionalRepository;

    public PeriodoConfigService(PeriodoAcademicoRepository periodoAcademicoRepository,
                                ConfiguracionInstitucionalRepository configuracionInstitucionalRepository) {
        this.periodoAcademicoRepository = periodoAcademicoRepository;
        this.configuracionInstitucionalRepository = configuracionInstitucionalRepository;
    }

    public List<PeriodoAcademico> listarPeriodos() {
        List<PeriodoAcademico> list = periodoAcademicoRepository.findAllByOrderByNumeroPeriodoAsc();
        if (list.isEmpty()) {
            list = List.of(
                    periodoAcademicoRepository.save(new PeriodoAcademico(1, "Primer Período", new BigDecimal("30.00"), LocalDate.parse("2026-02-01"), LocalDate.parse("2026-06-15"), "Activo")),
                    periodoAcademicoRepository.save(new PeriodoAcademico(2, "Segundo Período", new BigDecimal("35.00"), LocalDate.parse("2026-07-15"), LocalDate.parse("2026-09-15"), "Activo")),
                    periodoAcademicoRepository.save(new PeriodoAcademico(3, "Tercer Período", new BigDecimal("35.00"), LocalDate.parse("2026-09-16"), LocalDate.parse("2026-11-30"), "Activo"))
            );
        }
        return list;
    }

    public Map<Integer, BigDecimal> getPonderaciones() {
        Map<Integer, BigDecimal> map = new HashMap<>();
        List<PeriodoAcademico> periodos = listarPeriodos();
        for (PeriodoAcademico p : periodos) {
            map.put(p.getNumeroPeriodo(), p.getPesoPorcentaje());
        }
        map.putIfAbsent(1, new BigDecimal("30.00"));
        map.putIfAbsent(2, new BigDecimal("35.00"));
        map.putIfAbsent(3, new BigDecimal("35.00"));
        return map;
    }

    @Transactional
    public synchronized void actualizarPonderaciones(BigDecimal p1, BigDecimal p2, BigDecimal p3,
                                                     String fIni1, String fFin1,
                                                     String fIni2, String fFin2,
                                                     String fIni3, String fFin3) {
        actualizarPeriodoIndividual(1, "Primer Período", p1, fIni1, fFin1);
        actualizarPeriodoIndividual(2, "Segundo Período", p2, fIni2, fFin2);
        actualizarPeriodoIndividual(3, "Tercer Período", p3, fIni3, fFin3);
    }

    private void actualizarPeriodoIndividual(int numero, String nombre, BigDecimal peso, String fIni, String fFin) {
        PeriodoAcademico periodo = periodoAcademicoRepository.findByNumeroPeriodo(numero)
                .orElse(new PeriodoAcademico(numero, nombre, peso != null ? peso : BigDecimal.valueOf(33.33), LocalDate.now(), LocalDate.now(), "Activo"));

        if (peso != null) periodo.setPesoPorcentaje(peso);
        if (fIni != null && !fIni.isBlank()) {
            try { periodo.setFechaInicio(LocalDate.parse(fIni.trim())); } catch (DateTimeParseException ignored) {}
        }
        if (fFin != null && !fFin.isBlank()) {
            try { periodo.setFechaFin(LocalDate.parse(fFin.trim())); } catch (DateTimeParseException ignored) {}
        }
        periodoAcademicoRepository.save(periodo);
    }

    public ConfiguracionInstitucional obtenerConfiguracionInstitucional() {
        return configuracionInstitucionalRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseGet(() -> configuracionInstitucionalRepository.save(new ConfiguracionInstitucional(
                        "800.124.567-2",
                        "Institución Educativa Ateneo de la Ciencia e Innovación (IEACI)",
                        "San José de los Campanos, Mz 32 Lote 9-11",
                        "300 987 6543",
                        "contacto@ieaci.edu.co",
                        "2026"
                )));
    }

    @Transactional
    public void guardarConfiguracionInstitucional(String nit, String nombre, String direccion, String telefono, String correo, String anoLectivo) {
        ConfiguracionInstitucional cfg = obtenerConfiguracionInstitucional();
        if (nit != null && !nit.isBlank()) cfg.setNit(nit.trim());
        if (nombre != null && !nombre.isBlank()) cfg.setNombreInst(nombre.trim());
        if (direccion != null) cfg.setDireccionInst(direccion.trim());
        if (telefono != null) cfg.setTelefonoInst(telefono.trim());
        if (correo != null) cfg.setCorreoInst(correo.trim());
        if (anoLectivo != null && !anoLectivo.isBlank()) cfg.setAnoLectivo(anoLectivo.trim());
        configuracionInstitucionalRepository.save(cfg);
    }

    public BigDecimal getPesoPeriodo1() {
        return periodoAcademicoRepository.findByNumeroPeriodo(1).map(PeriodoAcademico::getPesoPorcentaje).orElse(new BigDecimal("30.00"));
    }
    public BigDecimal getPesoPeriodo2() {
        return periodoAcademicoRepository.findByNumeroPeriodo(2).map(PeriodoAcademico::getPesoPorcentaje).orElse(new BigDecimal("35.00"));
    }
    public BigDecimal getPesoPeriodo3() {
        return periodoAcademicoRepository.findByNumeroPeriodo(3).map(PeriodoAcademico::getPesoPorcentaje).orElse(new BigDecimal("35.00"));
    }

    public String getFechaInicioP1() {
        return periodoAcademicoRepository.findByNumeroPeriodo(1).map(p -> p.getFechaInicio().toString()).orElse("2026-02-01");
    }
    public String getFechaFinP1() {
        return periodoAcademicoRepository.findByNumeroPeriodo(1).map(p -> p.getFechaFin().toString()).orElse("2026-06-15");
    }
    public String getFechaInicioP2() {
        return periodoAcademicoRepository.findByNumeroPeriodo(2).map(p -> p.getFechaInicio().toString()).orElse("2026-07-15");
    }
    public String getFechaFinP2() {
        return periodoAcademicoRepository.findByNumeroPeriodo(2).map(p -> p.getFechaFin().toString()).orElse("2026-09-15");
    }
    public String getFechaInicioP3() {
        return periodoAcademicoRepository.findByNumeroPeriodo(3).map(p -> p.getFechaInicio().toString()).orElse("2026-09-16");
    }
    public String getFechaFinP3() {
        return periodoAcademicoRepository.findByNumeroPeriodo(3).map(p -> p.getFechaFin().toString()).orElse("2026-11-30");
    }
}
