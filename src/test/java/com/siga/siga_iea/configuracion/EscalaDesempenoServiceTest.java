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
    @DisplayName("Debe clasificar correctamente notas en los 6 niveles de desempeño incluyendo Crítico (0-1)")
    void testClasificacionSeisNiveles() {
        // 1. Superior (4.60 - 5.00)
        EscalaDesempeno sup = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("4.85"));
        assertEquals("SUPERIOR", sup.getCodigo());
        assertEquals("Desempeño Superior", sup.getNombre());
        assertTrue(sup.getColorBadge().contains("bg-sidebar"));

        // 2. Alto (4.00 - 4.59)
        EscalaDesempeno alto = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("4.20"));
        assertEquals("ALTO", alto.getCodigo());
        assertEquals("Desempeño Alto", alto.getNombre());
        assertTrue(alto.getColorBadge().contains("bg-emerald-600"));

        // 3. Básico (3.00 - 3.99)
        EscalaDesempeno basico = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("3.40"));
        assertEquals("BASICO", basico.getCodigo());
        assertEquals("Desempeño Básico", basico.getNombre());
        assertTrue(basico.getColorBadge().contains("bg-amber-500"));

        // 4. Bajo (2.00 - 2.99)
        EscalaDesempeno bajo = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("2.50"));
        assertEquals("BAJO", bajo.getCodigo());
        assertEquals("Desempeño Bajo", bajo.getNombre());
        assertTrue(bajo.getColorBadge().contains("bg-orange-500"));

        // 5. Muy Bajo (1.00 - 1.99)
        EscalaDesempeno muyBajo = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("1.30"));
        assertEquals("MUY_BAJO", muyBajo.getCodigo());
        assertEquals("Desempeño Muy Bajo", muyBajo.getNombre());
        assertTrue(muyBajo.getColorBadge().contains("bg-rose-500"));

        // 6. Crítico (0.00 - 0.99)
        EscalaDesempeno critico = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("0.75"));
        assertEquals("CRITICO", critico.getCodigo());
        assertEquals("Desempeño Crítico", critico.getNombre());
        assertTrue(critico.getColorBadge().contains("bg-red-700"));

        // Casos extremos
        EscalaDesempeno cero = escalaDesempenoService.obtenerEscalaPorNota(BigDecimal.ZERO);
        assertEquals("CRITICO", cero.getCodigo());

        EscalaDesempeno cinco = escalaDesempenoService.obtenerEscalaPorNota(new BigDecimal("5.00"));
        assertEquals("SUPERIOR", cinco.getCodigo());
    }
}
