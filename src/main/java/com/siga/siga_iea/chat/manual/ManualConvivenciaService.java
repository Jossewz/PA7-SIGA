package com.siga.siga_iea.chat.manual;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Catálogo normativo estructurado del Manual de Convivencia y SIEE
 * de la Institución Educativa Ambientalista de Cartagena de Indias (IEACI).
 * Permite resolución de consultas públicas sin transferir datos de menores a terceros.
 */
@Service
public class ManualConvivenciaService {

    public record ArticuloNormativo(
            String codigo,
            String capitulo,
            String titulo,
            String contenido,
            List<String> palabrasClave
    ) {}

    private final List<ArticuloNormativo> articulos = new ArrayList<>();

    public ManualConvivenciaService() {
        inicializarBaseNormativa();
    }

    private void inicializarBaseNormativa() {
        articulos.add(new ArticuloNormativo(
                "ART-FALTA-TIPO-1",
                "Capítulo V: Régimen Disciplinario y Convivencia Escolar",
                "Faltas Tipo I (Leves)",
                "Son aquellas situaciones esporádicas que inciden negativamente en el clima escolar y que no atentan contra el cuerpo o la salud física. " +
                        "Ejemplos: impuntualidad injustificada a clases o actos de comunidad, uso inadecuado de dispositivos móviles en horas lectivas sin autorización pedagógica, " +
                        "desorden en el aula o zonas comunes, porte incorrecto del uniforme institucional. " +
                        "Acción pedagógica: Diálogo formativo, amonestación verbal, compromiso reflexivo en el observador del estudiante.",
                List.of("falta leve", "tipo 1", "tipo i", "celular", "tarde", "impuntualidad", "uniforme", "desorden")
        ));

        articulos.add(new ArticuloNormativo(
                "ART-FALTA-TIPO-2",
                "Capítulo V: Régimen Disciplinario y Convivencia Escolar",
                "Faltas Tipo II (Graves)",
                "Situaciones de agresión escolar, acoso escolar (bullying) o ciberacoso que no revistan características de la comisión de un delito. " +
                        "Ejemplos: agresión verbal o discriminación reiterada, desacato flagrante a directivas o docentes, fraude comprobado en evaluaciones académicas, " +
                        "evasión del colegio o de clases sin autorización, daño intencional a la infraestructura o recursos del colegio. " +
                        "Acción pedagógica: Citación formal a acudientes, suscripción de acta de compromiso convivencial, trabajo pedagógico reparatorio, suspensión temporal hasta por 3 días.",
                List.of("falta grave", "tipo 2", "tipo ii", "bullying", "agresion", "fraude", "evasion", "dano", "pelea", "acoso")
        ));

        articulos.add(new ArticuloNormativo(
                "ART-FALTA-TIPO-3",
                "Capítulo V: Régimen Disciplinario y Convivencia Escolar",
                "Faltas Tipo III (Gravísimas - Ley 1620 de 2013)",
                "Situaciones que constituyen presunta comisión de delitos contra la libertad, integridad y formación sexual, lesiones personales graves o porte/distribución de sustancias psicoactivas o armas. " +
                        "Protocolo obligatorio: Atención médica inmediata si se requiere, remisión inmediata a Policía de Infancia y Adolescencia / ICBF, activación del Comité de Convivencia Escolar, notificación urgente a padres o acudientes legales.",
                List.of("falta gravisima", "tipo 3", "tipo iii", "delito", "drogas", "sustancias", "armas", "abuso", "policia", "icbf")
        ));

        articulos.add(new ArticuloNormativo(
                "ART-SIEE-ESCALA",
                "Capítulo VI: Sistema Institucional de Evaluación de los Estudiantes (SIEE)",
                "Escala Valorativa Institucional (Decreto 1290 de 2009)",
                "La IEACI califica el rendimiento académico en escala numérica de 1.00 a 5.00 con la siguiente equivalencia valorativa: " +
                        "• Desempeño Superior: 4.60 a 5.00 (alcanza la totalidad de propósitos sin dificultades). " +
                        "• Desempeño Alto: 4.00 a 4.59 (alcanza los propósitos con desempeño destacado). " +
                        "• Desempeño Básico: 3.00 a 3.99 (supera los propósitos mínimos esenciales). " +
                        "• Desempeño Bajo: 1.00 a 2.99 (no alcanza los propósitos mínimos; requiere plan de mejoramiento).",
                List.of("escala", "notas", "nota minima", "siee", "aprobado", "reprobado", "desempeno", "calificacion", "recuperacion")
        ));

        articulos.add(new ArticuloNormativo(
                "ART-SIEE-ASISTENCIA",
                "Capítulo VI: Sistema Institucional de Evaluación de los Estudiantes (SIEE)",
                "Criterio de Asistencia y Pérdida por Inasistencia",
                "La inasistencia injustificada igual o superior al 25% de las horas efectivas de clase en una asignatura durante el año escolar es causal de reprobación de la materia. " +
                        "Las fallas justificadas deben ser soportadas por el acudiente con constancia médica o calamidad doméstica dentro de los 3 días hábiles siguientes al reintegro.",
                List.of("asistencia", "fallas", "inasistencia", "perder por fallas", "justificar falla", "porcentaje inasistencia")
        ));

        articulos.add(new ArticuloNormativo(
                "ART-DEBIDO-PROCESO",
                "Capítulo VII: Garantías y Debido Proceso Formativo",
                "Rutas y Garantías del Debido Proceso",
                "Todo estudiante tiene derecho a: 1. Conocer de manera clara e inmediata los hechos imputados. " +
                        "2. Presentar sus descargos y pruebas en compañía de su acudiente. " +
                        "3. Presunción de inocencia. " +
                        "4. Interponer recursos de reposición y apelación ante el Rector o Consejo Directivo dentro de los términos establecidos.",
                List.of("debido proceso", "descargos", "derecho a la defensa", "apelacion", "sancion", "citacion")
        ));
    }

    public List<ArticuloNormativo> buscar(String consulta) {
        if (consulta == null || consulta.isBlank()) {
            return Collections.emptyList();
        }
        String normalizada = normalizarTexto(consulta);
        List<String> palabras = Arrays.asList(normalizada.split("\\s+"));

        return articulos.stream()
                .map(art -> new AbstractMap.SimpleEntry<>(art, calcularRelevancia(art, palabras)))
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private int calcularRelevancia(ArticuloNormativo art, List<String> palabrasConsulta) {
        int score = 0;
        String texto = normalizarTexto(art.titulo() + " " + art.contenido());
        for (String p : palabrasConsulta) {
            if (p.length() < 3) continue;
            if (texto.contains(p)) score += 2;
            for (String kw : art.palabrasClave()) {
                if (kw.contains(p) || p.contains(kw)) score += 5;
            }
        }
        return score;
    }

    private String normalizarTexto(String str) {
        return str.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9\\s]", " ");
    }
}
