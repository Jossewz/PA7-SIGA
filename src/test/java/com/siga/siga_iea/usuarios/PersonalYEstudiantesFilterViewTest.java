package com.siga.siga_iea.usuarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PersonalYEstudiantesFilterViewTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /estudiantes debe renderizar buscador, estado y botones filtrar/limpiar con flex-1")
    void testRenderEstudiantesFiltros() throws Exception {
        mockMvc.perform(get("/estudiantes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Buscar por nombre, documento o código")))
                .andExpect(content().string(containsString("name=\"search\"")))
                .andExpect(content().string(containsString("name=\"estado\"")))
                .andExpect(content().string(containsString("flex-1 h-10 px-4 text-xs font-black text-white bg-sidebar")))
                .andExpect(content().string(containsString("flex-1 h-10 px-4 text-xs font-black text-text-secondary bg-[#f1f5f1]")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /personal debe renderizar buscador, estado y botones filtrar/limpiar con flex-1")
    void testRenderPersonalFiltros() throws Exception {
        mockMvc.perform(get("/personal"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Buscar por nombre, documento o cargo")))
                .andExpect(content().string(containsString("name=\"search\"")))
                .andExpect(content().string(containsString("name=\"estado\"")))
                .andExpect(content().string(containsString("flex-1 h-10 px-4 text-xs font-black text-white bg-sidebar")))
                .andExpect(content().string(containsString("flex-1 h-10 px-4 text-xs font-black text-text-secondary bg-[#f1f5f1]")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /personal con parámetros de búsqueda y estado debe responder 200 OK")
    void testPersonalFiltrosConParametros() throws Exception {
        mockMvc.perform(get("/personal")
                        .param("search", "Perez")
                        .param("estado", "Activo"))
                .andExpect(status().isOk());
    }
}
