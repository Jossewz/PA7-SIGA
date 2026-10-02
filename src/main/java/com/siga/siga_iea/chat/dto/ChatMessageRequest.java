package com.siga.siga_iea.chat.dto;

public record ChatMessageRequest(
        String mensaje,
        String conversacionId
) {}
