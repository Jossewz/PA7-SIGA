package com.plataforma.colegio.usuarios.service;

import com.plataforma.colegio.usuarios.entity.Usuario;
import com.plataforma.colegio.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> findAll() {
        return Collections.emptyList();
    }
}
