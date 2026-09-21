package com.siga.siga_iea.auth.security;

import com.siga.siga_iea.auth.enums.RolEnum;
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

        RolEnum rolEnum = RolEnum.from(usuario.getRol());

        return new User(
                usuario.getEmail(),
                usuario.getPassword() != null ? usuario.getPassword() : "",
                Collections.singletonList(new SimpleGrantedAuthority(rolEnum.getAuthority()))
        );
    }

    public static String normalizeRole(String rawRole) {
        return RolEnum.from(rawRole).name();
    }
}

