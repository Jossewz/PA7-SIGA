package com.siga.siga_iea.configuracion;

import com.siga.siga_iea.configuracion.entity.AnioLectivo;
import com.siga.siga_iea.configuracion.entity.PeriodoAcademico;
import com.siga.siga_iea.configuracion.repository.AnioLectivoRepository;
import com.siga.siga_iea.configuracion.repository.PeriodoAcademicoRepository;
import com.siga.siga_iea.configuracion.service.PeriodoConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AnioLectivoPeriodoTest {

    @Autowired
    private AnioLectivoRepository anioLectivoRepository;

    @Autowired
    private PeriodoAcademicoRepository periodoAcademicoRepository;

    @Autowired
    private PeriodoConfigService periodoConfigService;

    @Test
    @Transactional
    @DisplayName("Debe permitir que dos anios lectivos distintos tengan un Periodo 1 sin conflicto de unicidad")
    void testPeriodosMismoNumeroEnDistintosAnios() {
        int year1 = 2098;
        int year2 = 2099;

        AnioLectivo anio2026 = anioLectivoRepository.save(new AnioLectivo(
                year1, "ACTIVO", LocalDate.of(year1, 1, 15), LocalDate.of(year1, 11, 30), true
        ));

        AnioLectivo anio2027 = anioLectivoRepository.save(new AnioLectivo(
                year2, "PLANIFICACION", LocalDate.of(year2, 1, 15), LocalDate.of(year2, 11, 30), false
        ));

        PeriodoAcademico p1_2026 = periodoAcademicoRepository.save(new PeriodoAcademico(
                anio2026, 1, "Primer Período " + year1, new BigDecimal("30.00"),
                LocalDate.of(year1, 2, 1), LocalDate.of(year1, 6, 15), "Activo"
        ));

        PeriodoAcademico p1_2027 = periodoAcademicoRepository.save(new PeriodoAcademico(
                anio2027, 1, "Primer Período " + year2, new BigDecimal("30.00"),
                LocalDate.of(year2, 2, 1), LocalDate.of(year2, 6, 15), "Planificación"
        ));

        assertNotNull(p1_2026.getId());
        assertNotNull(p1_2027.getId());
        assertNotEquals(p1_2026.getId(), p1_2027.getId());

        List<PeriodoAcademico> periodos2026 = periodoAcademicoRepository.findByAnioLectivoOrderByNumeroPeriodoAsc(anio2026);
        List<PeriodoAcademico> periodos2027 = periodoAcademicoRepository.findByAnioLectivoOrderByNumeroPeriodoAsc(anio2027);

        assertTrue(periodos2026.stream().anyMatch(p -> p.getNumeroPeriodo().equals(1) && p.getNombre().contains(String.valueOf(year1))));
        assertTrue(periodos2027.stream().anyMatch(p -> p.getNumeroPeriodo().equals(1) && p.getNombre().contains(String.valueOf(year2))));
    }

    @Test
    @Transactional
    @DisplayName("PeriodoConfigService debe resolver el anio lectivo actual y sus periodos correspondientes")
    void testPeriodoConfigServiceResuelveAnioActual() {
        AnioLectivo actual = periodoConfigService.obtenerAnioLectivoActual();
        assertNotNull(actual);
        assertTrue(actual.getEsActual());

        List<PeriodoAcademico> periodos = periodoConfigService.listarPeriodos();
        assertFalse(periodos.isEmpty());
        for (PeriodoAcademico p : periodos) {
            assertNotNull(p.getAnioLectivo());
            assertEquals(actual.getId(), p.getAnioLectivo().getId());
        }
    }
}
