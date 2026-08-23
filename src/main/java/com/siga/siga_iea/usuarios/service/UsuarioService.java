package com.siga.siga_iea.usuarios.service;

import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(UUID id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorNumeroDocumento(String doc) {
        if (doc == null || doc.isBlank()) return Optional.empty();
        return usuarioRepository.findByNumeroDocumento(doc.trim());
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return usuarioRepository.findByEmail(email.trim());
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$") && !usuario.getPassword().startsWith("$2b$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarEstado(UUID id, String nuevoEstado) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setEstado(nuevoEstado);
            usuarioRepository.save(u);
        });
    }

    @Transactional
    public void cambiarPassword(UUID id, String nuevaPassword) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setPassword(passwordEncoder.encode(nuevaPassword));
            usuarioRepository.save(u);
        });
    }

    @Transactional
    public void eliminar(UUID id) {
        usuarioRepository.deleteById(id);
    }
}
