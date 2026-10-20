package com.siga.siga_iea.calificaciones;

import com.siga.siga_iea.calificaciones.dto.BoletinMateriaDTO;
import com.siga.siga_iea.calificaciones.service.BoletinPdfService;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoletinPdfServiceTest {

    private final BoletinPdfService pdfService = new BoletinPdfService();

    @Test
    @DisplayName("Debe generar un PDF válido con el encabezado %PDF y bytes no vacíos")
    void debeGenerarPdfValido() {
        Estudiante estudiante = new Estudiante();
        estudiante.setNombres("Carlos Andrés");
        estudiante.setApellidos("Pérez Gómez");
        estudiante.setNumeroDocumento("1098765432");

        List<BoletinMateriaDTO> boletin = new ArrayList<>();
        
        BoletinMateriaDTO m1 = new BoletinMateriaDTO("cm1", "m1", "Matemáticas");
        m1.setPeriodo1Nota(new BigDecimal("4.50"));
        m1.setPeriodo2Nota(new BigDecimal("4.00"));
        m1.setPeriodo3Nota(new BigDecimal("4.20"));
        m1.setNotaDefinitiva(new BigDecimal("4.23"));
        m1.setDesempenoNombre("Alto");
        boletin.add(m1);

        BoletinMateriaDTO m2 = new BoletinMateriaDTO("cm2", "m2", "Lengua Castellana");
        m2.setPeriodo1Nota(new BigDecimal("3.80"));
        m2.setPeriodo2Nota(new BigDecimal("3.50"));
        m2.setPeriodo3Nota(new BigDecimal("3.70"));
        m2.setNotaDefinitiva(new BigDecimal("3.67"));
        m2.setDesempenoNombre("Básico");
        boletin.add(m2);

        byte[] pdfBytes = pdfService.generarBoletinPdf(
                estudiante,
                "10-01",
                "2026",
                boletin
        );

        assertNotNull(pdfBytes, "El arreglo de bytes del PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 500, "El PDF generado debe tener un tamaño razonable");

        // Validar firma mágica de archivo PDF (%PDF)
        String header = new String(pdfBytes, 0, 4);
        assertEquals("%PDF", header, "El documento generado debe comenzar con la cabecera estándar %PDF");
    }
}
