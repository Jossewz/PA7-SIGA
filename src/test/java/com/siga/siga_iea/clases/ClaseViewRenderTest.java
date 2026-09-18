package com.siga.siga_iea.clases;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClaseViewRenderTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.siga.siga_iea.usuarios.repository.EstudianteRepository estudianteRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /clases debe renderizar la vista sin errores de Thymeleaf")
    void testRenderClasesIndex() throws Exception {
        mockMvc.perform(get("/clases"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /clases/gestion debe renderizar la vista de detalle sin errores")
    void testRenderClasesGestion() throws Exception {
        mockMvc.perform(get("/clases/gestion").param("codigo", "11-01"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /clases/fragmento/tabla-notas debe renderizar el fragmento HTMX correctamente")
    void testRenderFragmentoTablaNotas() throws Exception {
        // Obtenemos el curso creado previamente o buscamos 11-01
        var mvcResult = mockMvc.perform(get("/clases/gestion").param("codigo", "11-01"))
                .andExpect(status().isOk())
                .andReturn();
        String cursoIdStr = (String) mvcResult.getModelAndView().getModel().get("cursoId");

        mockMvc.perform(get("/clases/fragmento/tabla-notas")
                        .param("cursoId", cursoIdStr)
                        .param("materiaNombre", "Matemáticas")
                        .param("periodo", "1")
                        .param("fecha", "2026-03-01"))
                .andExpect(status().isOk());
    }

    @Autowired
    private com.siga.siga_iea.clases.repository.ClaseRepository claseRepository;

    @Autowired
    private com.siga.siga_iea.clases.repository.CursoEstudianteRepository cursoEstudianteRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /calificaciones debe renderizar el boletin de calificaciones sin errores de Thymeleaf")
    void testRenderCalificaciones() throws Exception {
        mockMvc.perform(get("/calificaciones"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /calificaciones con estudiante y asignaturas debe renderizar filas, acordeon y detalles")
    void testRenderCalificacionesConDatos() throws Exception {
        // Creamos estudiante
        com.siga.siga_iea.usuarios.entity.Estudiante est = new com.siga.siga_iea.usuarios.entity.Estudiante();
        est.setNombres("Valentina");
        est.setApellidos("Gómez");
        est.setNumeroDocumento("DOC-BOL-" + System.currentTimeMillis());
        est = estudianteRepository.save(est);

        // Obtenemos un curso existente
        java.util.List<com.siga.siga_iea.clases.entity.Clase> clases = claseRepository.findAll();
        if (!clases.isEmpty()) {
            var curso = clases.get(0);
            com.siga.siga_iea.clases.entity.CursoEstudiante ce = new com.siga.siga_iea.clases.entity.CursoEstudiante();
            ce.setCurso(curso);
            ce.setEstudiante(est);
            ce.setAnoLectivo("2026");
            cursoEstudianteRepository.save(ce);
        }

        mockMvc.perform(get("/calificaciones").param("estudianteId", est.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Valentina Gómez")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Boletín de Calificaciones")));
    }

    @Autowired
    private com.siga.siga_iea.clases.service.ClaseService claseService;

    @Autowired
    private com.siga.siga_iea.clases.repository.MateriaRepository materiaRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /clases/calificaciones/guardar debe procesar decimales con punto y coma correctamente")
    void testGuardarCalificacionConDecimales() throws Exception {
        var mvcResult = mockMvc.perform(get("/clases/gestion").param("codigo", "11-01"))
                .andExpect(status().isOk())
                .andReturn();
        String cursoIdStr = (String) mvcResult.getModelAndView().getModel().get("cursoId");

        // Aseguramos que el curso tenga un horario configurado para el día actual
        com.siga.siga_iea.clases.entity.Materia mat = materiaRepository.findByNombre("Matemáticas")
                .orElseGet(() -> materiaRepository.save(new com.siga.siga_iea.clases.entity.Materia("Matemáticas", "Ciencias Exactas")));
        String diaHoy = com.siga.siga_iea.clases.controller.ClaseController.obtenerNombreDiaEspanol(java.time.LocalDate.now().getDayOfWeek());
        claseService.guardarHorarioBloque(java.util.UUID.fromString(cursoIdStr), diaHoy, mat.getId(), null, "07:00", "08:30", "Aula 101");

        // Crear una evaluacion
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/clases/evaluaciones/crear")
                        .param("cursoId", cursoIdStr)
                        .param("materiaNombre", "Matemáticas")
                        .param("periodo", "1"))
                .andExpect(status().isOk());

        // Obtener fragmento para recuperar IDs
        var fragmentResult = mockMvc.perform(get("/clases/fragmento/tabla-notas")
                        .param("cursoId", cursoIdStr)
                        .param("materiaNombre", "Matemáticas")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andReturn();

        java.util.List<?> evaluaciones = (java.util.List<?>) fragmentResult.getModelAndView().getModel().get("evaluaciones");
        java.util.UUID cursoMateriaId = (java.util.UUID) fragmentResult.getModelAndView().getModel().get("cursoMateriaId");
        org.junit.jupiter.api.Assertions.assertFalse(evaluaciones.isEmpty());
        com.siga.siga_iea.calificaciones.entity.Evaluacion ev = (com.siga.siga_iea.calificaciones.entity.Evaluacion) evaluaciones.get(0);

        // Insertamos un estudiante para probar
        com.siga.siga_iea.usuarios.entity.Estudiante est = new com.siga.siga_iea.usuarios.entity.Estudiante();
        est.setNombres("Carlos");
        est.setApellidos("Prueba");
        est.setNumeroDocumento("DOC-TEST-" + System.currentTimeMillis());
        est = estudianteRepository.save(est);
        
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/clases/calificaciones/guardar")
                        .param("evaluacionId", ev.getId().toString())
                        .param("estudianteId", est.getId().toString())
                        .param("nota", "4,75")
                        .param("cursoMateriaId", cursoMateriaId.toString())
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("badge-final-")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.containsString("4.75"), org.hamcrest.Matchers.containsString("4,75"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /clases/fragmento/tabla-notas sin horario asignado debe mostrar aviso y bloquear acciones")
    void testRenderFragmentoSinHorarioBloqueado() throws Exception {
        var mvcResult = mockMvc.perform(get("/clases/gestion").param("codigo", "11-01"))
                .andExpect(status().isOk())
                .andReturn();
        String cursoIdStr = (String) mvcResult.getModelAndView().getModel().get("cursoId");
        var curso = claseRepository.findById(java.util.UUID.fromString(cursoIdStr)).orElseThrow();

        // Creamos un estudiante asociado al curso para probar los botones bloqueados
        com.siga.siga_iea.usuarios.entity.Estudiante est = new com.siga.siga_iea.usuarios.entity.Estudiante();
        est.setNombres("Estudiante");
        est.setApellidos("Bloqueado");
        est.setNumeroDocumento("DOC-BLQ-" + System.currentTimeMillis());
        est = estudianteRepository.save(est);

        com.siga.siga_iea.clases.entity.CursoEstudiante ce = new com.siga.siga_iea.clases.entity.CursoEstudiante();
        ce.setCurso(curso);
        ce.setEstudiante(est);
        ce.setAnoLectivo("2026");
        cursoEstudianteRepository.save(ce);

        // Consultamos en una fecha de fin de semana (domingo 2026-03-01) sin horario
        mockMvc.perform(get("/clases/fragmento/tabla-notas")
                        .param("cursoId", cursoIdStr)
                        .param("periodo", "1")
                        .param("fecha", "2026-03-01"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Sin horario asignado para esta fecha")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Bloqueado")));
    }

    @Autowired
    private com.siga.siga_iea.usuarios.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private com.siga.siga_iea.usuarios.repository.DocenteRepository docenteRepository;

    @Test
    @WithMockUser(username = "admin@ieaci.edu.co", roles = "ADMIN")
    @DisplayName("GET /clases/gestion como ADMIN debe renderizar el <select id='select-materia'> y los botones de auto-mapear y promover")
    void testRenderClasesGestionModoAdmin() throws Exception {
        mockMvc.perform(get("/clases/gestion").param("codigo", "11-01"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("<select")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("id=\"select-materia\"")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Auto-Mapear Matriculados")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Promover Año Lectivo")));
    }

    @Test
    @DisplayName("GET /clases/gestion como DOCENTE NO debe mostrar el <select> ni los botones de auto-mapear y promover")
    void testRenderClasesGestionModoDocente() throws Exception {
        String docEmail = "docente.test." + System.currentTimeMillis() + "@ieaci.edu.co";
        String numDoc = "DOC-TEST-" + System.currentTimeMillis();

        com.siga.siga_iea.usuarios.entity.Docente docEntity = new com.siga.siga_iea.usuarios.entity.Docente();
        docEntity.setNombres("Profesor");
        docEntity.setApellidos("Prueba");
        docEntity.setNumeroDocumento(numDoc);
        docenteRepository.save(docEntity);

        com.siga.siga_iea.usuarios.entity.Usuario userEntity = new com.siga.siga_iea.usuarios.entity.Usuario();
        userEntity.setEmail(docEmail);
        userEntity.setRol("DOCENTE");
        userEntity.setNumeroDocumento(numDoc);
        userEntity.setPassword("secret");
        usuarioRepository.save(userEntity);

        mockMvc.perform(get("/clases/gestion")
                        .param("codigo", "11-01")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(docEmail).roles("DOCENTE")))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("badge-materia-estatica")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("type=\"hidden\" id=\"select-materia\"")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<select id=\"select-materia\""))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Auto-Mapear Matriculados"))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Promover Año Lectivo"))));
    }

    @Test
    @DisplayName("GET /clases/gestion como PERSONAL_ADMINISTRATIVO debe mostrar los botones de auto-mapear y promover")
    void testRenderClasesGestionModoAdministrativo() throws Exception {
        String adminEmail = "admin.sec." + System.currentTimeMillis() + "@ieaci.edu.co";
        String numDoc = "ADMIN-TEST-" + System.currentTimeMillis();

        com.siga.siga_iea.usuarios.entity.Usuario userEntity = new com.siga.siga_iea.usuarios.entity.Usuario();
        userEntity.setEmail(adminEmail);
        userEntity.setRol("PERSONAL_ADMINISTRATIVO");
        userEntity.setNumeroDocumento(numDoc);
        userEntity.setPassword("secret");
        usuarioRepository.save(userEntity);

        mockMvc.perform(get("/clases/gestion")
                        .param("codigo", "11-01")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(adminEmail).roles("PERSONAL_ADMINISTRATIVO")))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Auto-Mapear Matriculados")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Promover Año Lectivo")));
    }
}
