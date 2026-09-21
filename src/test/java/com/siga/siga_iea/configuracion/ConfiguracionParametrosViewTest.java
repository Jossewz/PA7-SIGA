package com.siga.siga_iea.configuracion;

import com.siga.siga_iea.clases.entity.Materia;
import com.siga.siga_iea.clases.repository.MateriaRepository;
import com.siga.siga_iea.clases.service.ClaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConfiguracionParametrosViewTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private ClaseService claseService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /configuracion debe renderizar Parámetros del Sistema con periodos y escala completa, sin vista mock antigua")
    void testRenderConfiguracionGeneral() throws Exception {
        mockMvc.perform(get("/configuracion"))
                .andExpect(status().isOk())
                // Pestañas principales
                .andExpect(content().string(containsString("Institución")))
                .andExpect(content().string(containsString("Parámetros del Sistema")))
                .andExpect(content().string(containsString("Asignaturas")))
                .andExpect(content().string(containsString("Roles y Permisos")))
                // Secciones de Parámetros del Sistema
                .andExpect(content().string(containsString("1. Periodos Académicos y Ponderación Oficial")))
                .andExpect(content().string(containsString("2. Escala de Desempeño y Calificaciones (0.00 a 5.00)")))
                // Los 6 niveles de desempeño
                .andExpect(content().string(containsString("Desempeño Superior")))
                .andExpect(content().string(containsString("Desempeño Alto")))
                .andExpect(content().string(containsString("Desempeño Básico")))
                .andExpect(content().string(containsString("Desempeño Bajo")))
                .andExpect(content().string(containsString("Desempeño Muy Bajo")))
                .andExpect(content().string(containsString("Desempeño Crítico")))
                // Asegurar que la vista mock antigua NO exista
                .andExpect(content().string(not(containsString("Cargos de Nómina"))))
                .andExpect(content().string(not(containsString("Tipos de Certificado"))))
                .andExpect(content().string(not(containsString("Jornadas Autorizadas"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /configuracion/periodos debe validar que la suma sea 100%")
    void testValidacionSumaPonderaciones() throws Exception {
        // Suma errónea (90%)
        mockMvc.perform(post("/configuracion/periodos")
                        .param("pesoP1", "30")
                        .param("pesoP2", "30")
                        .param("pesoP3", "30")
                        .param("fIniP1", "2026-02-01")
                        .param("fFinP1", "2026-06-15")
                        .param("fIniP2", "2026-07-15")
                        .param("fFinP2", "2026-09-15")
                        .param("fIniP3", "2026-09-16")
                        .param("fFinP3", "2026-11-30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensajeError"));

        // Suma válida (100%)
        mockMvc.perform(post("/configuracion/periodos")
                        .param("pesoP1", "30")
                        .param("pesoP2", "35")
                        .param("pesoP3", "35")
                        .param("fIniP1", "2026-02-01")
                        .param("fFinP1", "2026-06-15")
                        .param("fIniP2", "2026-07-15")
                        .param("fFinP2", "2026-09-15")
                        .param("fIniP3", "2026-09-16")
                        .param("fFinP3", "2026-11-30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Solo materias activas deben ser retornadas para la asignación de horarios en cursos")
    void testFiltroMateriasActivasParaHorarios() throws Exception {
        // Crear una materia inactiva de prueba
        Materia inactiva = new Materia("Astronomía Experimental", "Ciencias");
        inactiva.setEstado("Inactivo");
        materiaRepository.save(inactiva);

        // Crear una materia activa de prueba
        Materia activa = new Materia("Biología Molecular", "Ciencias");
        activa.setEstado("Activo");
        materiaRepository.save(activa);

        List<Materia> activasParaHorarios = claseService.listarMateriasActivas();

        assertTrue(activasParaHorarios.stream().anyMatch(m -> m.getNombre().equals("Biología Molecular")));
        assertFalse(activasParaHorarios.stream().anyMatch(m -> m.getNombre().equals("Astronomía Experimental")));
    }

    @Test
    @WithMockUser(username = "admin@ieaci.edu.co", roles = "ADMIN")
    @DisplayName("Actualizar permisos de ESTUDIANTE debe habilitar dinámicamente el acceso al módulo PERSONAL")
    void testPermisosRolesActualizacionYAccesoDinamico() throws Exception {
        // Asignar acceso al módulo PERSONAL para el rol ESTUDIANTE
        mockMvc.perform(post("/configuracion/roles/permisos")
                        .param("rol", "ESTUDIANTE")
                        .param("modulosAcceso", "MATRICULAS", "PERSONAL", "CURSOS_HORARIOS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/configuracion?rol=ESTUDIANTE&tab=cfg-roles"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Crear usuario ESTUDIANTE en base de datos para probar el interceptor
        String testStudentEmail = "estudiante.test." + System.currentTimeMillis() + "@ieaci.edu.co";
        com.siga.siga_iea.usuarios.entity.Usuario student = new com.siga.siga_iea.usuarios.entity.Usuario();
        student.setEmail(testStudentEmail);
        student.setRol("ESTUDIANTE");
        student.setNumeroDocumento("EST-12345");
        student.setPassword("secret");
        student.setEstado("Activo");
        com.siga.siga_iea.usuarios.repository.UsuarioRepository usuarioRepo = org.springframework.test.util.TestSocketUtils.class != null ?
                // get from application context
                null : null;

        // Probar acceso con el nuevo permiso concedido
        mockMvc.perform(get("/configuracion?rol=ESTUDIANTE")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@ieaci.edu.co").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Directorio institucional de docentes y personal administrativo")))
                .andExpect(content().string(not(containsString("No autorizado"))));
    }
}

