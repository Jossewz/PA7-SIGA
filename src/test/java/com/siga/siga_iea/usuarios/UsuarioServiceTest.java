package com.siga.siga_iea.usuarios;

import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import com.siga.siga_iea.usuarios.service.UsuarioService;
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

    @Test
    @DisplayName("buscarPorEmail() debe retornar el usuario cuando existe")
    void buscarPorEmail_cuandoExiste_retornaUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@colegio.edu.co");
        when(usuarioRepository.findByEmail("test@colegio.edu.co")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorEmail("test@colegio.edu.co");

        assertTrue(resultado.isPresent());
        assertEquals("test@colegio.edu.co", resultado.get().getEmail());
        verify(usuarioRepository).findByEmail("test@colegio.edu.co");
    }

    @Test
    @DisplayName("buscarPorEmail() con null o blanco debe retornar Optional.empty sin consultar el repo")
    void buscarPorEmail_cuandoEsBlanco_retornaEmpty() {
        Optional<Usuario> resNull = usuarioService.buscarPorEmail(null);
        Optional<Usuario> resBlank = usuarioService.buscarPorEmail("   ");

        assertTrue(resNull.isEmpty());
        assertTrue(resBlank.isEmpty());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("guardar() debe encriptar la contraseña si viene en texto plano")
    void guardar_encriptaPasswordPlano() {
        Usuario usuario = new Usuario();
        usuario.setPassword("miClaveSecreta123");

        when(passwordEncoder.encode("miClaveSecreta123")).thenReturn("$2a$10$hashedPasswordMock");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuario);

        assertEquals("$2a$10$hashedPasswordMock", guardado.getPassword());
        verify(passwordEncoder).encode("miClaveSecreta123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("guardar() NO debe re-encriptar la contraseña si ya tiene formato BCrypt ($2a$ o $2b$)")
    void guardar_noReencriptaPasswordBcrypt() {
        Usuario usuario = new Usuario();
        usuario.setPassword("$2a$10$alreadyHashedPassword");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuario);

        assertEquals("$2a$10$alreadyHashedPassword", guardado.getPassword());
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("cambiarEstado() debe actualizar el estado del usuario")
    void cambiarEstado_actualizaEstadoCorrectamente() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEstado("ACTIVO");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.cambiarEstado(id, "INACTIVO");

        assertEquals("INACTIVO", usuario.getEstado());
        verify(usuarioRepository).save(usuario);
    }
}
