package com.siga.siga_iea.chat.dto;

import java.util.List;

public record ChatMessageResponse(
        String respuesta,
        String rolAutorizado,
        List<String> herramientasInvocadas,
        List<String> referenciasNormativas,
        long latenciaMs,
        boolean procesadoLocalmente,
        String proveedor
) {
    public ChatMessageResponse(String respuesta,
                               String rolAutorizado,
                               List<String> herramientasInvocadas,
                               List<String> referenciasNormativas,
                               long latenciaMs,
                               boolean procesadoLocalmente) {
        this(respuesta, rolAutorizado, herramientasInvocadas, referenciasNormativas, latenciaMs, procesadoLocalmente,
                procesadoLocalmente ? "LLM Local" : "Motor Normativo Interno");
    }
}
