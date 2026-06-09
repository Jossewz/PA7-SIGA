package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}

