package com.siga.siga_iea.calificaciones.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.siga.siga_iea.calificaciones.dto.BoletinMateriaDTO;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio generador de Boletines Oficiales de Calificaciones en formato PDF (OpenPDF).
 * Cumple con los lineamientos del Decreto 1290 de 2009 y el SIEACI de la IEACI.
 */
@Service
public class BoletinPdfService {

    // Paleta institucional SIGA
    private static final Color COLOR_PRIMARIO = new Color(13, 65, 23);       // #0d4117 verde bosque
    private static final Color COLOR_SECUNDARIO = new Color(234, 251, 228);   // #eafbe4 verde claro
    private static final Color COLOR_BORDE = new Color(196, 238, 192);        // #c4eec0 verde menta
    private static final Color COLOR_ZEBRA = new Color(247, 252, 246);        // #f7fcf6 fondo filas alternas
    private static final Color COLOR_TEXTO = new Color(30, 41, 59);           // slate-800
    private static final Color COLOR_ALERT_ROJO = new Color(185, 28, 28);     // reprobado

    public byte[] generarBoletinPdf(Estudiante estudiante, String grado, String anioLectivo, List<BoletinMateriaDTO> materias) {
        Document document = new Document(PageSize.A4, 30, 30, 35, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Encabezado Institucional
            agregarEncabezado(document);

            // 2. Información del Estudiante y Curso
            agregarInfoEstudiante(document, estudiante, grado, anioLectivo);

            // 3. Tabla de Calificaciones por Asignatura
            agregarTablaCalificaciones(document, materias);

            // 4. Resumen y Firmas
            agregarResumenYFirmas(document, materias);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF del boletín oficial: " + e.getMessage(), e);
        }
    }

    private void agregarEncabezado(Document doc) throws DocumentException {
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARIO);
        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXTO);
        Font fontMini = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.DARK_GRAY);

        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.setLeading(12);

        p.add(new Chunk("REPÚBLICA DE COLOMBIA\n", fontMini));
        p.add(new Chunk("SECRETARÍA DE EDUCACIÓN DISTRITAL DE CARTAGENA\n", fontSub));
        p.add(new Chunk("INSTITUCIÓN EDUCATIVA AMBIENTAL DE CARTAGENA - IEACI\n", fontTitulo));
        p.add(new Chunk("DANE: 113001002345 | NIT: 806.012.345-1 | Resolución de Aprobación No. 0845\n", fontMini));
        p.add(new Chunk("SISTEMA INTEGRAL DE GESTIÓN ACADÉMICA (SIGA) — BOLETÍN INFORMATIVO DE EVALUACIÓN\n\n", fontSub));

        doc.add(p);
    }

    private void agregarInfoEstudiante(Document doc, Estudiante est, String grado, String anioLectivo) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(0);
        table.setSpacingAfter(10);
        table.setWidths(new float[]{25, 35, 18, 22});

        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_PRIMARIO);
        Font fontVal = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, COLOR_TEXTO);

        agregarCeldaClaveValor(table, "ESTUDIANTE:", est.getNombreCompleto(), fontHeader, fontVal);
        agregarCeldaClaveValor(table, "DOCUMENTO:", (est.getTipoDocumento() != null ? est.getTipoDocumento() : "TI") + " " + est.getNumeroDocumento(), fontHeader, fontVal);
        agregarCeldaClaveValor(table, "GRADO / GRUPO:", grado != null ? grado : "Sin Grado", fontHeader, fontVal);
        agregarCeldaClaveValor(table, "AÑO LECTIVO:", anioLectivo != null ? anioLectivo : "2026", fontHeader, fontVal);
        agregarCeldaClaveValor(table, "FECHA EMISIÓN:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontHeader, fontVal);
        agregarCeldaClaveValor(table, "ESTADO:", est.getEstado() != null ? est.getEstado() : "Activo", fontHeader, fontVal);

        doc.add(table);
    }

    private void agregarCeldaClaveValor(PdfPTable table, String clave, String valor, Font fClave, Font fVal) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_ZEBRA);
        cell.setBorderColor(COLOR_BORDE);
        cell.setPadding(4.5f);

        Paragraph p = new Paragraph();
        p.add(new Chunk(clave + " ", fClave));
        p.add(new Chunk(valor != null ? valor : "-", fVal));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void agregarTablaCalificaciones(Document doc, List<BoletinMateriaDTO> materias) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{38, 12, 12, 12, 12, 14});

        Font fHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
        String[] headers = {"ASIGNATURA / ÁREA", "P1 (33%)", "P2 (33%)", "P3 (34%)", "DEF.", "DESEMPEÑO"};

        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, fHeader));
            c.setBackgroundColor(COLOR_PRIMARIO);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setPadding(5);
            table.addCell(c);
        }

        Font fMateria = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_TEXTO);
        Font fNota = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXTO);
        Font fNotaBaja = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_ALERT_ROJO);
        Font fNotaAlta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_PRIMARIO);

        boolean alternate = false;
        for (BoletinMateriaDTO m : materias) {
            Color rowBg = alternate ? COLOR_ZEBRA : Color.WHITE;
            alternate = !alternate;

            // Asignatura
            PdfPCell cNombre = new PdfPCell(new Phrase(m.getMateriaNombre(), fMateria));
            cNombre.setBackgroundColor(rowBg);
            cNombre.setBorderColor(COLOR_BORDE);
            cNombre.setPadding(4);
            table.addCell(cNombre);

            // P1
            table.addCell(crearCeldaNota(m.getPeriodo1Nota(), fNota, fNotaBaja, rowBg));
            // P2
            table.addCell(crearCeldaNota(m.getPeriodo2Nota(), fNota, fNotaBaja, rowBg));
            // P3
            table.addCell(crearCeldaNota(m.getPeriodo3Nota(), fNota, fNotaBaja, rowBg));

            // Definitiva
            BigDecimal def = m.getNotaDefinitiva();
            Font fDef = (def != null && def.compareTo(new BigDecimal("3.0")) < 0) ? fNotaBaja : fNotaAlta;
            table.addCell(crearCeldaNota(def, fDef, fNotaBaja, rowBg));

            // Desempeño
            String des = m.getDesempenoNombre() != null ? m.getDesempenoNombre() : "-";
            PdfPCell cDes = new PdfPCell(new Phrase(des, fDef));
            cDes.setBackgroundColor(rowBg);
            cDes.setBorderColor(COLOR_BORDE);
            cDes.setHorizontalAlignment(Element.ALIGN_CENTER);
            cDes.setPadding(4);
            table.addCell(cDes);
        }

        doc.add(table);
    }

    private PdfPCell crearCeldaNota(BigDecimal nota, Font fNormal, Font fBaja, Color bg) {
        String texto = (nota != null) ? nota.setScale(2, RoundingMode.HALF_UP).toString() : "-";
        Font f = (nota != null && nota.compareTo(new BigDecimal("3.0")) < 0) ? fBaja : fNormal;

        PdfPCell cell = new PdfPCell(new Phrase(texto, f));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(COLOR_BORDE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        return cell;
    }

    private void agregarResumenYFirmas(Document doc, List<BoletinMateriaDTO> materias) throws DocumentException {
        // Cálculo de promedio general
        BigDecimal suma = BigDecimal.ZERO;
        int count = 0;
        for (BoletinMateriaDTO m : materias) {
            if (m.getNotaDefinitiva() != null) {
                suma = suma.add(m.getNotaDefinitiva());
                count++;
            }
        }

        BigDecimal promedioGeneral = count > 0 ? suma.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, COLOR_PRIMARIO);

        Paragraph pResumen = new Paragraph();
        pResumen.setSpacingBefore(8);
        pResumen.add(new Chunk("PROMEDIO GENERAL ACUMULADO: " + promedioGeneral + " / 5.00  •  TOTAL ASIGNATURAS: " + materias.size() + "\n", fBold));
        Font fNorma = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.DARK_GRAY);
        pResumen.add(new Chunk("Escala de Valoración Nacional (Decreto 1290 / SIEACI): Superior (4.6 - 5.0) | Alto (4.0 - 4.5) | Básico (3.0 - 3.9) | Bajo (1.0 - 2.9)\n", fNorma));
        doc.add(pResumen);

        // Bloque de Firmas
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setSpacingBefore(35);
        firmas.setWidths(new float[]{50, 50});

        Font fFirma = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXTO);

        PdfPCell cFirma1 = new PdfPCell();
        cFirma1.setBorder(Rectangle.NO_BORDER);
        cFirma1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cFirma1.addElement(new Paragraph("____________________________________\nRECTOR(A) INSTITUCIONAL\nC.C. Directiva IEACI", fFirma));
        firmas.addCell(cFirma1);

        PdfPCell cFirma2 = new PdfPCell();
        cFirma2.setBorder(Rectangle.NO_BORDER);
        cFirma2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cFirma2.addElement(new Paragraph("____________________________________\nDIRECTOR(A) DE GRUPO\nDocente Asignado IEACI", fFirma));
        firmas.addCell(cFirma2);

        doc.add(firmas);
    }
}
