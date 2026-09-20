package com.siga.siga_iea.usuarios;

import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstudianteServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @InjectMocks
    private EstudianteService estudianteService;

    @Test
    @DisplayName("guardar() genera código secuencial y resuelve colisiones si el código candidato ya existe en BD")
    void guardar_resuelveColisionDeCodigo() {
        int currentYear = Year.now().getValue();
        String collisionCode = String.format("%d004", currentYear);
        String resolvedCode = String.format("%d005", currentYear);

        // Simulamos que count() da 3, por lo que candidate = year + "004"
        when(estudianteRepository.count()).thenReturn(3L);
        // La BD indica que collisionCode ya existe, pero resolvedCode está libre
        when(estudianteRepository.existsByCodigo(eq(collisionCode))).thenReturn(true);
        when(estudianteRepository.existsByCodigo(eq(resolvedCode))).thenReturn(false);

        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(inv -> inv.getArgument(0));

        Estudiante nuevo = new Estudiante();
        nuevo.setNombres("Carlos");
        nuevo.setApellidos("Pérez");
        nuevo.setNumeroDocumento("1020304050");

        Estudiante guardado = estudianteService.guardar(nuevo);

        assertNotNull(guardado.getCodigo());
        assertEquals(resolvedCode, guardado.getCodigo(), "Debe saltar el código colisionado y asignar el siguiente libre");
        verify(estudianteRepository).existsByCodigo(collisionCode);
        verify(estudianteRepository).existsByCodigo(resolvedCode);
        verify(estudianteRepository).save(nuevo);
    }

    @Test
    @DisplayName("guardar() no sobreescribe el código si el estudiante ya tiene uno asignado")
    void guardar_mantieneCodigoExistente() {
        Estudiante existente = new Estudiante();
        existente.setNombres("Ana");
        existente.setApellidos("Gómez");
        existente.setNumeroDocumento("987654321");
        existente.setCodigo("2025099");

        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(inv -> inv.getArgument(0));

        Estudiante guardado = estudianteService.guardar(existente);

        assertEquals("2025099", guardado.getCodigo());
        verify(estudianteRepository, never()).count();
        verify(estudianteRepository, never()).existsByCodigo(anyString());
        verify(estudianteRepository).save(existente);
    }

    @Test
    @DisplayName("guardar() lanza IllegalArgumentException si faltan datos obligatorios")
    void guardar_validaCamposObligatorios() {
        Estudiante sinNombre = new Estudiante();
        sinNombre.setNumeroDocumento("12345");
        assertThrows(IllegalArgumentException.class, () -> estudianteService.guardar(sinNombre));

        Estudiante sinDocumento = new Estudiante();
        sinDocumento.setNombres("Juan");
        assertThrows(IllegalArgumentException.class, () -> estudianteService.guardar(sinDocumento));
    }
}
