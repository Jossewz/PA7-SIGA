package com.siga.siga_iea.usuarios;

import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import com.siga.siga_iea.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioDemo;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        usuarioDemo = new Usuario("docente@ieaci.edu.co", "password123", "DOCENTE", "12345678");
        usuarioDemo.setId(usuarioId);
        usuarioDemo.setEstado("Activo");
    }

    @Test
    @DisplayName("guardar() debe encriptar la contraseña con BCrypt si está en texto plano")
    void guardar_encriptaPasswordEnTextoPlano() {
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedHashPassword123");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuarioDemo);

        assertNotNull(guardado);
        assertEquals("$2a$10$encodedHashPassword123", guardado.getPassword());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(usuarioDemo);
    }

    @Test
    @DisplayName("guardar() NO debe re-encriptar la contraseña si ya tiene hash BCrypt ($2a$)")
    void guardar_noReEncriptaPasswordConHash() {
        usuarioDemo.setPassword("$2a$10$alreadyHashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuarioDemo);

        assertEquals("$2a$10$alreadyHashedPassword", guardado.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, times(1)).save(usuarioDemo);
    }

    @Test
    @DisplayName("buscarPorEmail() debe retornar el usuario cuando existe")
    void buscarPorEmail_retornaUsuarioExistente() {
        when(usuarioRepository.findByEmail("docente@ieaci.edu.co")).thenReturn(Optional.of(usuarioDemo));

        Optional<Usuario> resultado = usuarioService.buscarPorEmail("docente@ieaci.edu.co");

        assertTrue(resultado.isPresent());
        assertEquals("docente@ieaci.edu.co", resultado.get().getEmail());
        assertEquals("DOCENTE", resultado.get().getRol());
        verify(usuarioRepository, times(1)).findByEmail("docente@ieaci.edu.co");
    }

    @Test
    @DisplayName("cambiarPassword() debe encriptar la nueva contraseña y actualizar")
    void cambiarPassword_encriptaYGuardaNuevaPassword() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioDemo));
        when(passwordEncoder.encode("nuevaClave2026")).thenReturn("$2a$10$nuevaClaveHash2026");

        usuarioService.cambiarPassword(usuarioId, "nuevaClave2026");

        assertEquals("$2a$10$nuevaClaveHash2026", usuarioDemo.getPassword());
        verify(usuarioRepository, times(1)).save(usuarioDemo);
    }
}
