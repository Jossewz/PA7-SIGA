package com.siga.siga_iea.config;

import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        inicializarSuperAdmin();
        migrarContraseñasTextoPlano();
    }

    private void inicializarSuperAdmin() {
        String adminEmail = "admin@ieaci.edu.co";
        Optional<Usuario> adminOpt = usuarioRepository.findByEmail(adminEmail);

        if (adminOpt.isEmpty()) {
            log.info("Creando usuario Super Admin por defecto ({})", adminEmail);
            Usuario admin = new Usuario();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRol("ADMIN");
            admin.setNumeroDocumento("0000000000");
            admin.setEstado("Activo");
            usuarioRepository.save(admin);
            log.info("Super Admin creado con éxito.");
        } else {
            Usuario admin = adminOpt.get();
            if (admin.getPassword() == null || !admin.getPassword().startsWith("$2a$")) {
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRol("ADMIN");
                usuarioRepository.save(admin);
                log.info("Contraseña de Super Admin actualizada a BCrypt.");
            }
        }
    }

    private void migrarContraseñasTextoPlano() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        int migrados = 0;
        for (Usuario u : usuarios) {
            String pwd = u.getPassword();
            if (pwd != null && !pwd.isBlank() && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
                u.setPassword(passwordEncoder.encode(pwd));
                usuarioRepository.save(u);
                migrados++;
            }
        }
        if (migrados > 0) {
            log.info("Se migraron {} contraseñas en texto plano a BCrypt.", migrados);
        }
    }
}
