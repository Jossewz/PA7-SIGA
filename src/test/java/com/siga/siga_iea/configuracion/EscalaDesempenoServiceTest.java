package com.siga.siga_iea.configuracion;

import com.siga.siga_iea.configuracion.entity.EscalaDesempeno;
import com.siga.siga_iea.configuracion.service.EscalaDesempenoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EscalaDesempenoServiceTest {

    @Autowired
    private EscalaDesempenoService escalaDesempenoService;

    @Test
    @DisplayName("Debe clasificar notas estrictamente en los 4 niveles oficiales del SIEACI (Ítem 5.4.11 / Decreto 1290) con redondeo a una cifra decimal")
    void testClasificacionCuatroNivelesSieaci() {
        // 1. Superior (4.6 a 5.0)
        EscalaDesempeno sup = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("4.85"));
        assertEquals("SUPERIOR", sup.getCodigo());
        assertEquals("Desempeño Superior", sup.getNombre());
        assertTrue(sup.getColorBadge().contains("bg-sidebar"));

        // Redondeo de borde hacia Superior: 4.55 se redondea a 4.6
        EscalaDesempeno supBorde = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("4.55"));
        assertEquals("SUPERIOR", supBorde.getCodigo());

        // 2. Alto (4.0 a 4.5)
        EscalaDesempeno alto = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("4.20"));
        assertEquals("ALTO", alto.getCodigo());
        assertEquals("Desempeño Alto", alto.getNombre());
        assertTrue(alto.getColorBadge().contains("bg-emerald-600"));

        // Redondeo de borde hacia Alto: 3.95 se redondea a 4.0; 4.54 se redondea a 4.5
        EscalaDesempeno altoBordeInf = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("3.95"));
        assertEquals("ALTO", altoBordeInf.getCodigo());
        EscalaDesempeno altoBordeSup = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("4.54"));
        assertEquals("ALTO", altoBordeSup.getCodigo());

        // 3. Básico (3.0 a 3.9)
        EscalaDesempeno basico = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("3.40"));
        assertEquals("BASICO", basico.getCodigo());
        assertEquals("Desempeño Básico", basico.getNombre());
        assertTrue(basico.getColorBadge().contains("bg-amber-500"));

        // Redondeo de borde hacia Básico: 2.95 se redondea a 3.0; 3.94 se redondea a 3.9
        EscalaDesempeno basicoBordeInf = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("2.95"));
        assertEquals("BASICO", basicoBordeInf.getCodigo());
        EscalaDesempeno basicoBordeSup = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("3.94"));
        assertEquals("BASICO", basicoBordeSup.getCodigo());

        // 4. Bajo (1.0 a 2.9)
        EscalaDesempeno bajo = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("2.50"));
        assertEquals("BAJO", bajo.getCodigo());
        assertEquals("Desempeño Bajo", bajo.getNombre());
        assertTrue(bajo.getColorBadge().contains("bg-rose-500"));

        EscalaDesempeno bajoBorde = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("1.00"));
        assertEquals("BAJO", bajoBorde.getCodigo());

        // 5. Casos extremos y notas por debajo de 1.0:
        EscalaDesempeno critico = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("0.75"));
        assertEquals("BAJO", critico.getCodigo());

        EscalaDesempeno cero = escalaDesempenoService.obtenerEscalaPorNota(BigDecimal.ZERO);
        assertEquals("BAJO", cero.getCodigo());

        EscalaDesempeno cinco = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("5.00"));
        assertEquals("SUPERIOR", cinco.getCodigo());

        // Verificar que la escala completa contiene únicamente 4 niveles normativos
        java.util.List<EscalaDesempeno> lista = escalaDesempenoService.listarEscalaCompleta();
        assertEquals(4, lista.size(), "El SIEACI define exactamente 4 niveles de desempeño (Decreto 1290)");
        assertTrue(lista.stream().noneMatch(e -> "CRITICO".equalsIgnoreCase(e.getCodigo())));
        assertTrue(lista.stream().noneMatch(e -> "MUY_BAJO".equalsIgnoreCase(e.getCodigo())));
    }
}
