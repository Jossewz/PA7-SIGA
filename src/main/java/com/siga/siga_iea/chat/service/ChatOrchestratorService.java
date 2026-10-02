package com.siga.siga_iea.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siga.siga_iea.auth.service.CurrentUserContextService;
import com.siga.siga_iea.chat.dto.ChatMessageRequest;
import com.siga.siga_iea.chat.dto.ChatMessageResponse;
import com.siga.siga_iea.chat.dto.ToolExecutionResult;
import com.siga.siga_iea.chat.manual.ManualConvivenciaService;
import com.siga.siga_iea.chat.manual.ManualConvivenciaService.ArticuloNormativo;
import com.siga.siga_iea.chat.tools.ChatToolsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orquestador principal del Chatbot Escolar SIGA - IEACI.
 * Gestiona el ciclo de vida de la consulta:
 * 1. Detección de rol y autorización de herramientas mediante Spring Security.
 * 2. Consulta de la base de conocimiento normativa (Manual de Convivencia / SIEE).
 * 3. Ejecución de herramientas autorizadas de solo lectura.
 * 4. Invocación a LLM local (Ollama) con mecanismo Zero-Failure Fallback determinista.
 */
@Service
public class ChatOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestratorService.class);

    private final CurrentUserContextService userContextService;
    private final ChatToolsService toolsService;
    private final ManualConvivenciaService manualConvivenciaService;
    private final ChatPromptSafety promptSafety;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${siga.chat.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${siga.chat.ollama.model:llama3.2:1b}")
    private String ollamaModel;

    public ChatOrchestratorService(CurrentUserContextService userContextService,
                                   ChatToolsService toolsService,
                                   ManualConvivenciaService manualConvivenciaService,
                                   ChatPromptSafety promptSafety,
                                   ObjectMapper objectMapper) {
        this.userContextService = userContextService;
        this.toolsService = toolsService;
        this.manualConvivenciaService = manualConvivenciaService;
        this.promptSafety = promptSafety;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(1500));
        requestFactory.setReadTimeout(Duration.ofMillis(3500));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public ChatMessageResponse procesarMensaje(ChatMessageRequest request) {
        long t0 = System.currentTimeMillis();
        String mensaje = promptSafety.sanitizarEntradaUsuario(request.mensaje());
        String rol = userContextService.getRolAutenticado().name();

        List<String> herramientasInvocadas = new ArrayList<>();
        List<String> referenciasNormativas = new ArrayList<>();
        StringBuilder contextoDatos = new StringBuilder();

        // 1. Búsqueda normativa en Manual de Convivencia
        List<ArticuloNormativo> normativas = manualConvivenciaService.buscar(mensaje);
        if (!normativas.isEmpty()) {
            contextoDatos.append("NORMATIVA APLICABLE (Manual de Convivencia IEACI):\n");
            for (ArticuloNormativo art : normativas) {
                referenciasNormativas.add(art.codigo() + " - " + art.titulo());
                contextoDatos.append("- [").append(art.codigo()).append("] ").append(art.titulo())
                        .append(": ").append(art.contenido()).append("\n");
            }
            contextoDatos.append("\n");
        }

        // 2. Detección y ejecución de herramientas según rol
        ejecutarHerramientasPorIntencion(mensaje, rol, herramientasInvocadas, contextoDatos);

        // 3. Intento de generación con LLM local (Ollama)
        String promptCompleto = promptSafety.construirPromptConContexto(rol, contextoDatos.toString(), mensaje);
        Optional<String> respuestaLlm = intentarLlamadaOllama(promptCompleto);

        String respuestaFinal;
        boolean procesadoLocalmente;

        if (respuestaLlm.isPresent() && !respuestaLlm.get().isBlank()) {
            respuestaFinal = respuestaLlm.get();
            procesadoLocalmente = true;
        } else {
            // Fallback determinista: genera respuesta estructurada con los datos de las herramientas y normativa
            respuestaFinal = generarRespuestaFallback(mensaje, rol, normativas, contextoDatos.toString());
            procesadoLocalmente = false;
        }

        long latencia = System.currentTimeMillis() - t0;
        return new ChatMessageResponse(
                respuestaFinal,
                rol,
                herramientasInvocadas,
                referenciasNormativas,
                latencia,
                procesadoLocalmente
        );
    }

    private void ejecutarHerramientasPorIntencion(String mensaje, String rol, 
                                                  List<String> herramientasInvocadas, 
                                                  StringBuilder contexto) {
        String query = mensaje.toLowerCase();

        if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
            if (query.contains("nota") || query.contains("calificacion") || query.contains("periodo")) {
                try {
                    ToolExecutionResult res = toolsService.consultarMisNotas();
                    herramientasInvocadas.add(res.nombreHerramienta());
                    contexto.append("DATOS DE TUS CALIFICACIONES:\n").append(res.datos()).append("\n\n");
                } catch (Exception e) {
                    log.warn("Error ejecutando consultarMisNotas: {}", e.getMessage());
                }
            }
            if (query.contains("horario") || query.contains("clase hoy") || query.contains("salon")) {
                try {
                    ToolExecutionResult res = toolsService.consultarMiHorarioHoy();
                    herramientasInvocadas.add(res.nombreHerramienta());
                    contexto.append("DATOS DE TU HORARIO DE HOY:\n").append(res.datos()).append("\n\n");
                } catch (Exception e) {
                    log.warn("Error ejecutando consultarMiHorarioHoy: {}", e.getMessage());
                }
            }
            if (query.contains("asistencia") || query.contains("falla") || query.contains("falta")) {
                try {
                    ToolExecutionResult res = toolsService.consultarMiResumenAsistencia();
                    herramientasInvocadas.add(res.nombreHerramienta());
                    contexto.append("DATOS DE TU ASISTENCIA:\n").append(res.datos()).append("\n\n");
                } catch (Exception e) {
                    log.warn("Error ejecutando consultarMiResumenAsistencia: {}", e.getMessage());
                }
            }
        } else if ("DOCENTE".equalsIgnoreCase(rol)) {
            if (query.contains("curso") || query.contains("grupo") || query.contains("mis clases")) {
                try {
                    ToolExecutionResult res = toolsService.consultarMisCursos();
                    herramientasInvocadas.add(res.nombreHerramienta());
                    contexto.append("DATOS DE TUS CURSOS:\n").append(res.datos()).append("\n\n");
                } catch (Exception e) {
                    log.warn("Error ejecutando consultarMisCursos: {}", e.getMessage());
                }
            }
        } else if ("ADMIN".equalsIgnoreCase(rol) || "PERSONAL_ADMINISTRATIVO".equalsIgnoreCase(rol)) {
            if (query.contains("estadistica") || query.contains("matricula") || query.contains("total") || query.contains("colegio")) {
                try {
                    ToolExecutionResult res = toolsService.consultarEstadisticasGenerales();
                    herramientasInvocadas.add(res.nombreHerramienta());
                    contexto.append("ESTADÍSTICAS INSTITUCIONALES:\n").append(res.datos()).append("\n\n");
                } catch (Exception e) {
                    log.warn("Error ejecutando consultarEstadisticasGenerales: {}", e.getMessage());
                }
            }
        }
    }

    private Optional<String> intentarLlamadaOllama(String prompt) {
        try {
            Map<String, Object> payload = Map.of(
                    "model", ollamaModel,
                    "prompt", prompt,
                    "stream", false
            );

            String responseBody = restClient.post()
                    .uri(ollamaUrl + "/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            if (responseBody != null) {
                JsonNode root = objectMapper.readTree(responseBody);
                if (root.has("response")) {
                    return Optional.of(root.get("response").asText().trim());
                }
            }
        } catch (Exception e) {
            log.debug("Ollama local no disponible en {}: {}. Empleando fallback determinista.", ollamaUrl, e.getMessage());
        }
        return Optional.empty();
    }

    private String generarRespuestaFallback(String mensaje, String rol, 
                                             List<ArticuloNormativo> normativas, 
                                             String contexto) {
        StringBuilder sb = new StringBuilder();

        if (!normativas.isEmpty()) {
            sb.append("📋 **Orientación Normativa (Manual de Convivencia IEACI)**\n\n");
            for (ArticuloNormativo art : normativas) {
                sb.append("• **").append(art.titulo()).append("** (Ref: `").append(art.codigo()).append("`):\n");
                sb.append("  ").append(art.contenido()).append("\n\n");
            }
        }

        if (contexto.contains("DATOS DE TUS CALIFICACIONES")) {
            sb.append("📊 **Resumen de Calificaciones Registradas**\n");
            sb.append("Se consultó tu historial académico activo. Puedes consultar el detalle completo por materia en el módulo de calificaciones.\n\n");
        } else if (contexto.contains("DATOS DE TU HORARIO DE HOY")) {
            sb.append("⏰ **Horario de Clases para Hoy**\n");
            sb.append("Tu curso cuenta con franjas lectivas programadas para el día de hoy. Recuerda consultar tu salón asignado.\n\n");
        } else if (contexto.contains("DATOS DE TU ASISTENCIA")) {
            sb.append("📅 **Estado de Asistencia Institucional**\n");
            sb.append("Recuerda que según el SIEE, una inasistencia injustificada igual o superior al 25% puede comprometer la aprobación de la asignatura.\n\n");
        } else if (contexto.contains("ESTADÍSTICAS INSTITUCIONALES")) {
            sb.append("🏛️ **Panel Directivo IEACI**\n");
            sb.append("Información institucional consolidada para el año lectivo en curso.\n\n");
        }

        if (sb.isEmpty()) {
            sb.append("👋 Hola. Soy el Asistente Institucional del SIGA - IEACI.\n\n");
            sb.append("Actualmente tu rol autenticado es **").append(rol).append("**.\n\n");
            sb.append("Puedo orientarte sobre:\n");
            sb.append("1. **Manual de Convivencia y SIEE**: Faltas Tipo I, II y III, escalas valorativas y debido proceso.\n");
            if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
                sb.append("2. **Consultas Académicas**: Tus calificaciones, horario de hoy y resumen de asistencias.\n");
            } else if ("DOCENTE".equalsIgnoreCase(rol)) {
                sb.append("2. **Gestión Docente**: Cursos asignados, dirección de grupo y seguimiento de jornada.\n");
            } else {
                sb.append("2. **Gestión Directiva**: Estadísticas generales de matrícula y sedes institucionales.\n");
            }
        }

        return sb.toString().trim();
    }
}
