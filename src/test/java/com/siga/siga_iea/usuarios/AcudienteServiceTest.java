package com.siga.siga_iea.usuarios;

import com.siga.siga_iea.usuarios.entity.Acudiente;
import com.siga.siga_iea.usuarios.repository.AcudienteRepository;
import com.siga.siga_iea.usuarios.service.AcudienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcudienteServiceTest {

    @Mock
    private AcudienteRepository acudienteRepository;

    @InjectMocks
    private AcudienteService acudienteService;

    @Test
    @DisplayName("buscarOCrear() crea nuevo acudiente sin campo dirección cuando no existe")
    void buscarOCrear_creaNuevoAcudiente() {
        when(acudienteRepository.findByNumeroDocumento("12345678")).thenReturn(Optional.empty());
        when(acudienteRepository.save(any(Acudiente.class))).thenAnswer(inv -> inv.getArgument(0));

        Acudiente ac = acudienteService.buscarOCrear(
                "Martha", "Rodríguez", "Madre", "CC", "12345678", "3001234567"
        );

        assertNotNull(ac);
        assertEquals("Martha", ac.getNombres());
        assertEquals("Rodríguez", ac.getApellidos());
        assertEquals("Madre", ac.getParentesco());
        assertEquals("CC", ac.getTipoDocumento());
        assertEquals("12345678", ac.getNumeroDocumento());
        assertEquals("3001234567", ac.getTelefono());
        assertEquals("Martha Rodríguez", ac.getNombreCompleto());
        verify(acudienteRepository).save(any(Acudiente.class));
    }

    @Test
    @DisplayName("buscarOCrear() actualiza acudiente existente sin requerir dirección")
    void buscarOCrear_actualizaAcudienteExistente() {
        Acudiente existente = new Acudiente("Martha", "Silva", "Tía", "CC", "12345678", "3100000000");
        when(acudienteRepository.findByNumeroDocumento("12345678")).thenReturn(Optional.of(existente));
        when(acudienteRepository.save(any(Acudiente.class))).thenAnswer(inv -> inv.getArgument(0));

        Acudiente actualizado = acudienteService.buscarOCrear(
                "Martha Cecilia", "Rodríguez Silva", "Madre", "CC", "12345678", "3001234567"
        );

        assertEquals("Martha Cecilia", actualizado.getNombres());
        assertEquals("Rodríguez Silva", actualizado.getApellidos());
        assertEquals("Madre", actualizado.getParentesco());
        assertEquals("3001234567", actualizado.getTelefono());
        verify(acudienteRepository).save(existente);
    }
}
