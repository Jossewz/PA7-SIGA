package com.siga.siga_iea.auth.security;

import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if ("Inactivo".equalsIgnoreCase(usuario.getEstado())) {
            throw new UsernameNotFoundException("El usuario se encuentra inactivo");
        }

        String roleName = normalizeRole(usuario.getRol());

        return new User(
                usuario.getEmail(),
                usuario.getPassword() != null ? usuario.getPassword() : "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
    }

    public static String normalizeRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return "ESTUDIANTE";
        }
        String clean = rawRole.trim().toUpperCase().replace(" ", "_");
        if (clean.contains("ADMIN") && !clean.contains("PERSONAL")) {
            return "ADMIN";
        }
        if (clean.contains("PERSONAL") || clean.contains("RECTOR") || clean.contains("COORDINADOR") || clean.contains("SECRETARI")) {
            return "PERSONAL_ADMINISTRATIVO";
        }
        if (clean.contains("DOCENTE")) {
            return "DOCENTE";
        }
        if (clean.contains("ESTUDIANTE")) {
            return "ESTUDIANTE";
        }
        return clean;
    }
}

