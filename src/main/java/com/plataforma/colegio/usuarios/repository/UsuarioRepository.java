package com.plataforma.colegio.usuarios.repository;

import com.plataforma.colegio.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
