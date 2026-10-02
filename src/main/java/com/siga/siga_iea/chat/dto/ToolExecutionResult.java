package com.siga.siga_iea.chat.dto;

import java.util.Map;

public record ToolExecutionResult(
        String nombreHerramienta,
        boolean exitoso,
        String descripcion,
        Map<String, Object> datos
) {}
