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

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
class PersonalYEstudiantesFilterViewTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /estudiantes debe renderizar buscador, nivel, estado, botones compactos y popover flotante estilo SEGI")
    void testRenderEstudiantesFiltros() throws Exception {
        mockMvc.perform(get("/estudiantes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Buscar por nombre, documento o código")))
                .andExpect(content().string(containsString("name=\"search\"")))
                .andExpect(content().string(containsString("name=\"nivel\"")))
                .andExpect(content().string(containsString("name=\"estado\"")))
                .andExpect(content().string(containsString("x-data=\"{ showFilters: false }\"")))
                .andExpect(content().string(containsString("@click.outside=\"showFilters = false\"")))
                .andExpect(content().string(containsString("x-show=\"showFilters\"")))
                .andExpect(content().string(containsString("Aplicar filtro")))
                .andExpect(content().string(containsString("h-9 px-3.5 text-xs font-black text-white bg-sidebar")))
                .andExpect(content().string(containsString("h-9 px-3.5 text-xs font-black text-text-secondary bg-[#f1f5f1]")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /personal debe renderizar buscador, tipo, estado, botones compactos y popover flotante estilo SEGI")
    void testRenderPersonalFiltros() throws Exception {
        mockMvc.perform(get("/personal"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Buscar por nombre, documento o cargo")))
                .andExpect(content().string(containsString("name=\"search\"")))
                .andExpect(content().string(containsString("name=\"tipo\"")))
                .andExpect(content().string(containsString("name=\"estado\"")))
                .andExpect(content().string(containsString("x-data=\"{ showFilters: false }\"")))
                .andExpect(content().string(containsString("@click.outside=\"showFilters = false\"")))
                .andExpect(content().string(containsString("x-show=\"showFilters\"")))
                .andExpect(content().string(containsString("Aplicar filtro")))
                .andExpect(content().string(containsString("h-9 px-3.5 text-xs font-black text-white bg-sidebar")))
                .andExpect(content().string(containsString("h-9 px-3.5 text-xs font-black text-text-secondary bg-[#f1f5f1]")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET / (Dashboard) no debe contener la etiqueta 'En BD' y debe incluir el sidebar colapsable con Alpine.js")
    void testRenderDashboardSinEnBd() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Estudiantes Activos")))
                .andExpect(content().string(containsString("Docentes Activos")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("En BD"))))
                .andExpect(content().string(containsString("id=\"sidebar-toggle-btn\"")))
                .andExpect(content().string(containsString("@click.stop.prevent=\"toggleSidebar()\"")))
                .andExpect(content().string(containsString("alpine.min.js")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /matricula debe renderizar con sidebar colapsable, Alpine.js y Matrícula como ítem activo")
    void testRenderMatriculaSidebar() throws Exception {
        mockMvc.perform(get("/matricula"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Formulario de Matrícula")))
                .andExpect(content().string(containsString("id=\"sidebar-toggle-btn\"")))
                .andExpect(content().string(containsString("@click.stop.prevent=\"toggleSidebar()\"")))
                .andExpect(content().string(containsString("alpine.min.js")))
                .andExpect(content().string(containsString("href=\"/matricula\"\n               class=\"sidebar-nav-item is-active\"")))
                .andExpect(content().string(containsString("href=\"/\"\n               class=\"sidebar-nav-item\"")));
    }
}
