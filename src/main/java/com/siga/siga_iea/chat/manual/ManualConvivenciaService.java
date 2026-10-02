package com.siga.siga_iea.chat.manual;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Catálogo normativo oficial extraído del documento institucional aprobado:
 * "SIEACI - Sistema Institucional de Evaluación de los Estudiantes"
 * Institución Educativa Ambientalista de Cartagena de Indias (Código: GC-F05, Versión 04, Vigencia: 10/11/25).
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
        inicializarBaseNormativaOficial();
    }

    private void inicializarBaseNormativaOficial() {
        // 1. Escala valorativa institucional (Ítem 5.4.11)
        articulos.add(new ArticuloNormativo(
                "SIEACI-5.4.11",
                "Capítulo 5: Conceptualización, Criterios Institucionales de Evaluación, Promoción y Reprobación",
                "Escala de Valoración Institucional (Equivalencia Decreto 1290 de 2009)",
                "La escala de valoración institucional de la IEACI se expresa numéricamente de 1,0 a 5,0 con los siguientes desempeños: " +
                        "• Desempeño Superior: 4,6 a 5,0. " +
                        "• Desempeño Alto: 4,0 a 4,5. " +
                        "• Desempeño Básico: 3,0 a 3,9. " +
                        "• Desempeño Bajo: 1,0 a 2,9.",
                List.of("escala", "notas", "nota minima", "sieaci", "calificacion", "desempeno", "superior", "alto", "basico", "bajo")
        ));

        // 2. Ponderación por dimensiones (Ítem 5.4.4)
        articulos.add(new ArticuloNormativo(
                "SIEACI-5.4.4",
                "Capítulo 5: Criterios de Evaluación",
                "Valoración Porcentual por Dimensiones en Cada Periodo",
                "En el proceso formativo de la IEACI, la valoración porcentual en cada periodo académico se distribuye así: " +
                        "1. Dimensión Personal: 15%. " +
                        "2. Dimensión Socio-Ambiental: 15%. " +
                        "3. Dimensión Cognitivo-Procedimental: 70%. " +
                        "Estas dimensiones deben reflejarse en las actividades de aula, planillas e informes académicos.",
                List.of("dimensiones", "porcentaje", "personal", "socio-ambiental", "cognitivo", "procedimental", "evaluacion", "ponderacion")
        ));

        // 3. Criterios de promoción (Ítem 5.5.3 y 5.5.4)
        articulos.add(new ArticuloNormativo(
                "SIEACI-5.5.3",
                "Capítulo 5: Promoción",
                "Criterios de Promoción de Grado y Nivelación",
                "La promoción de un grado a otro se otorgará cuando el estudiante demuestre desempeño superior, alto o básico en todas las asignaturas " +
                        "con nota mínima de 3.00, previa Comisión de Evaluación y Promoción, exceptuando la enseñanza religiosa escolar (ítem 5.5.3). " +
                        "Los estudiantes que al finalizar el año persistan en una o dos asignaturas con desempeño bajo deberán realizar actividades " +
                        "pedagógicas especiales complementarias (nivelación) con compromiso formal del acudiente (ítem 5.5.4).",
                List.of("promocion", "pasar de ano", "pasar el grado", "aprobar grado", "nivelacion", "recuperacion final")
        ));

        // 4. Criterios de reprobación e inasistencia (Ítem 5.6.1)
        articulos.add(new ArticuloNormativo(
                "SIEACI-5.6.1",
                "Capítulo 5: Reprobación",
                "Causales de Reprobación del Grado e Inasistencia",
                "Se considera que un estudiante reprueba el grado en la IEACI cuando: " +
                        "1. Obtiene valoración final en nivel bajo (menor a 3.0) en tres (3) o más asignaturas. " +
                        "2. Ha dejado de asistir injustificadamente al 25% o más de las actividades académicas. " +
                        "3. Ha dejado de asistir justificadamente al 30% o más de las actividades académicas sin entrega de compromisos acordados.",
                List.of("reprobacion", "perder el ano", "perder por fallas", "inasistencia", "porcentaje fallas", "25%", "30%", "faltas de asistencia")
        ));

        // 5. Estructura de periodos del año escolar (Ítem 7.1)
        articulos.add(new ArticuloNormativo(
                "SIEACI-7.1",
                "Capítulo 7: Comisión de Evaluación y Promoción",
                "División y Ponderación de los Periodos Académicos",
                "El año escolar de la IEACI está dividido formalmente en tres (3) periodos académicos: " +
                        "• Primer periodo: 13 semanas con un valor del 33%. " +
                        "• Segundo periodo: 12 semanas con un valor del 33%. " +
                        "• Tercer periodo: 15 semanas con un valor del 34%.",
                List.of("periodos", "cuantos periodos", "semanas", "valor periodo", "33%", "34%", "calendario")
        ));

        // 6. Instancias de reclamación y apelación (Capítulo 11)
        articulos.add(new ArticuloNormativo(
                "SIEACI-CAP-11",
                "Capítulo 11: Instancias de Atención y Resolución de Reclamaciones y Apelación",
                "Conducto Regular y Términos de Respuesta para Reclamaciones",
                "Ante desacuerdos en procesos evaluativos, se debe seguir el siguiente conducto regular: " +
                        "1ª Instancia: Docente de la asignatura (plazo de respuesta: 3 días hábiles). " +
                        "2ª Instancia: Director de grupo (plazo de respuesta: 3 días hábiles). " +
                        "3ª Instancia: Coordinación Académica (plazo de respuesta: 5 días hábiles). " +
                        "4ª Instancia: Comisión de Evaluación y Promoción (plazo de respuesta: 5 días hábiles). " +
                        "5ª Instancia: Consejo Académico (plazo de respuesta: 5 días hábiles). " +
                        "Última Instancia: Consejo Directivo (plazo de respuesta: 5 días hábiles).",
                List.of("reclamo", "reclamacion", "apelacion", "segundo evaluador", "queja", "conducto regular", "plazo reclamo", "derecho a la defensa")
        ));

        // 7. Promoción anticipada (Ítem 7.3)
        articulos.add(new ArticuloNormativo(
                "SIEACI-7.3",
                "Capítulo 7: Comisión de Evaluación y Promoción",
                "Promoción Anticipada de Grado",
                "Durante el primer periodo académico, la Comisión recomendará ante el Consejo Académico la promoción anticipada " +
                        "de estudiantes que demuestren rendimiento superior excepcional en todas las áreas. Solo es posible en los grupos de grados: " +
                        "1° a 3°, 4° a 5°, 6° a 7° y 8° a 9°. Requiere consentimiento expreso del padre de familia o acudiente y acta de aprobación.",
                List.of("promocion anticipada", "saltar de grado", "avanzar de grado", "repitentes")
        ));

        // 8. Régimen disciplinario y faltas (Ley 1620 de 2013 y Manual de Convivencia)
        articulos.add(new ArticuloNormativo(
                "MANUAL-FALTAS-TIPO-1",
                "Manual de Convivencia - Régimen de Convivencia Escolar",
                "Faltas Tipo I (Leves)",
                "Situaciones esporádicas que inciden negativamente en el clima escolar sin vulnerar la salud física. " +
                        "Incluye: impuntualidad reiterada, uso indebido del celular en horas de clase sin autorización pedagógica, porte incorrecto del uniforme institucional y desorden en el aula. " +
                        "Acción: Diálogo formativo reflexivo, amonestación verbal y registro pedagógico en el observador.",
                List.of("falta leve", "tipo 1", "tipo i", "celular", "uniforme", "tarde", "impuntualidad", "desorden")
        ));

        articulos.add(new ArticuloNormativo(
                "MANUAL-FALTAS-TIPO-2",
                "Manual de Convivencia - Régimen de Convivencia Escolar",
                "Faltas Tipo II (Graves)",
                "Situaciones de agresión escolar, acoso escolar (bullying) o ciberacoso que no revistan características de delito. " +
                        "Incluye: agresión verbal sistemática, desacato a directivos/docentes, fraude académico comprobado, daño premeditado a bienes del colegio o evasión de clases. " +
                        "Acción: Citación a acudientes, suscripción de acta de compromiso, trabajo pedagógico reparatorio y suspensión de 1 a 3 días.",
                List.of("falta grave", "tipo 2", "tipo ii", "bullying", "pelea", "fraude", "evasion", "agresion", "dano")
        ));

        articulos.add(new ArticuloNormativo(
                "MANUAL-FALTAS-TIPO-3",
                "Manual de Convivencia - Régimen de Convivencia Escolar",
                "Faltas Tipo III (Gravísimas - Ley 1620 de 2013)",
                "Situaciones que constituyen presunta comisión de delitos (lesiones personales graves, porte de armas, distribución de sustancias psicoactivas o violencia sexual). " +
                        "Protocolo obligatorio: Atención médica inmediata si se requiere, activación de la ruta distrital con Policía de Infancia y Adolescencia / ICBF, remisión al Comité de Convivencia y notificación a acudientes.",
                List.of("falta gravisima", "tipo 3", "tipo iii", "armas", "drogas", "sustancias", "delito", "policia", "icbf")
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
