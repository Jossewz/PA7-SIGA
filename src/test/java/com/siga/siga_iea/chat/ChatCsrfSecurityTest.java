package com.siga.siga_iea.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siga.siga_iea.chat.dto.ChatMessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChatCsrfSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("CSRF Security: POST a /chat/api/mensaje sin token CSRF devuelve 403 Forbidden")
    @WithMockUser(username = "estudiante@ieaci.edu.co", roles = {"ESTUDIANTE"})
    void testPostChatApiSinCsrfDevuelve403Forbidden() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest("¿Qué es una falta tipo I?", null);

        mockMvc.perform(post("/chat/api/mensaje")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF Security: POST a /chat/api/mensaje con token CSRF válido devuelve 200 OK")
    @WithMockUser(username = "estudiante@ieaci.edu.co", roles = {"ESTUDIANTE"})
    void testPostChatApiConCsrfValidoDevuelve200Ok() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest("¿Qué es una falta tipo I?", null);

        mockMvc.perform(post("/chat/api/mensaje")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta").exists())
                .andExpect(jsonPath("$.rolAutorizado").value("ESTUDIANTE"));
    }

    @Test
    @DisplayName("Control de Acceso: POST a /chat/api/mensaje sin autenticación devuelve redirección a login")
    void testPostChatApiSinAutenticacionRechazado() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest("¿Qué es una falta tipo I?", null);

        mockMvc.perform(post("/chat/api/mensaje")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection());
    }
}
