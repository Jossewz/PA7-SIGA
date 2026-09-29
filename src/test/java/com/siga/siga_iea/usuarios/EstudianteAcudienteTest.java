package com.siga.siga_iea.usuarios;

import com.siga.siga_iea.usuarios.entity.Acudiente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.EstudianteAcudiente;
import com.siga.siga_iea.usuarios.repository.AcudienteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteAcudienteRepository;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EstudianteAcudienteTest {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private AcudienteRepository acudienteRepository;

    @Autowired
    private EstudianteAcudienteRepository estudianteAcudienteRepository;

    @Autowired
    private EstudianteService estudianteService;

    @Test
    @Transactional
    @DisplayName("Debe permitir múltiples acudientes por estudiante (Madre y Padre) y asignar el principal")
    void testMultiplesAcudientesPorEstudiante() {
        Estudiante est = new Estudiante();
        est.setNombres("Santiago");
        est.setApellidos("Gómez");
        est.setNumeroDocumento("1020304099");
        est = estudianteRepository.save(est);

        Acudiente madre = new Acudiente("María", "López", "Madre", "CC", "40111222", "3001112233");
        madre = acudienteRepository.save(madre);

        Acudiente padre = new Acudiente("Carlos", "Gómez", "Padre", "CC", "70111222", "3004445566");
        padre = acudienteRepository.save(padre);

        // Asociar madre como principal
        estudianteService.asociarAcudiente(est.getId(), madre.getId(), "Madre", true);
        // Asociar padre como secundario
        estudianteService.asociarAcudiente(est.getId(), padre.getId(), "Padre", false);

        List<EstudianteAcudiente> relaciones = estudianteService.listarAcudientesDeEstudiante(est.getId());
        assertEquals(2, relaciones.size(), "El estudiante debe tener 2 acudientes registrados");

        Optional<Acudiente> principalOpt = estudianteService.obtenerAcudientePrincipal(est.getId());
        assertTrue(principalOpt.isPresent());
        assertEquals("María López", principalOpt.get().getNombreCompleto());

        // Cambiar principal al padre
        estudianteService.asociarAcudiente(est.getId(), padre.getId(), "Padre", true);

        Optional<Acudiente> nuevoPrincipalOpt = estudianteService.obtenerAcudientePrincipal(est.getId());
        assertTrue(nuevoPrincipalOpt.isPresent());
        assertEquals("Carlos Gómez", nuevoPrincipalOpt.get().getNombreCompleto());
    }

    @Test
    @Transactional
    @DisplayName("Un mismo acudiente debe poder estar asociado a múltiples estudiantes (hermanos)")
    void testAcudienteCompartidoPorHermanos() {
        Acudiente madre = acudienteRepository.save(new Acudiente(
                "Elena", "Ramírez", "Madre", "CC", "52999888", "3109998877"
        ));

        Estudiante hermano1 = new Estudiante();
        hermano1.setNombres("Lucas");
        hermano1.setApellidos("Pardo");
        hermano1.setNumeroDocumento("1099887701");
        hermano1 = estudianteRepository.save(hermano1);

        Estudiante hermano2 = new Estudiante();
        hermano2.setNombres("Sofía");
        hermano2.setApellidos("Pardo");
        hermano2.setNumeroDocumento("1099887702");
        hermano2 = estudianteRepository.save(hermano2);

        estudianteService.asociarAcudiente(hermano1.getId(), madre.getId(), "Madre", true);
        estudianteService.asociarAcudiente(hermano2.getId(), madre.getId(), "Madre", true);

        List<EstudianteAcudiente> hijos = estudianteAcudienteRepository.findByAcudienteId(madre.getId());
        assertEquals(2, hijos.size(), "La madre debe tener vinculados a los 2 hermanos");
    }
}
