package com.siga.siga_iea.usuarios.repository;

import com.siga.siga_iea.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);
}
