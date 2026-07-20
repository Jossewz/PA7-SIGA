package com.siga.siga_iea.usuarios.service;

import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.PersonalAdministrativo;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.DocenteRepository;
import com.siga.siga_iea.usuarios.repository.PersonalAdministrativoRepository;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PersonalService {

    private final DocenteRepository docenteRepository;
    private final PersonalAdministrativoRepository personalAdministrativoRepository;
    private final UsuarioRepository usuarioRepository;

    public PersonalService(DocenteRepository docenteRepository,
                           PersonalAdministrativoRepository personalAdministrativoRepository,
                           UsuarioRepository usuarioRepository) {
        this.docenteRepository = docenteRepository;
        this.personalAdministrativoRepository = personalAdministrativoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Docente> listarDocentes() {
        return docenteRepository.findAll();
    }

    public List<PersonalAdministrativo> listarPersonalAdministrativo() {
        return personalAdministrativoRepository.findAll();
    }

    public List<Docente> buscarDocentes(String search, String estado) {
        return docenteRepository.searchDocentes(search, estado);
    }

    public List<PersonalAdministrativo> buscarPersonal(String search, String cargo, String area, String estado) {
        return personalAdministrativoRepository.searchPersonal(search, cargo, area, estado);
    }

    public Optional<Docente> buscarDocentePorId(UUID id) {
        return docenteRepository.findById(id);
    }

    public Optional<PersonalAdministrativo> buscarPersonalPorId(UUID id) {
        return personalAdministrativoRepository.findById(id);
    }

    @Transactional
    public Docente guardarDocente(Docente docente) {
        return docenteRepository.save(docente);
    }

    @Transactional
    public PersonalAdministrativo guardarPersonal(PersonalAdministrativo personal) {
        return personalAdministrativoRepository.save(personal);
    }

    public String generarEmailSugerido(String nombres, String apellidos) {
        String primerNombre = (nombres != null && !nombres.isBlank()) ? nombres.trim().split("\\s+")[0] : "personal";
        String primerApellido = (apellidos != null && !apellidos.isBlank()) ? apellidos.trim().split("\\s+")[0] : "ieaci";

        String base = slugify(primerNombre) + "." + slugify(primerApellido);
        String domain = "@ieaci.edu.co";
        String candidate = base + domain;

        int counter = 1;
        while (usuarioRepository.existsByEmail(candidate)) {
            candidate = base + counter + domain;
            counter++;
        }

        return candidate;
    }

    @Transactional
    public Usuario crearOCambiarCuentaAcceso(String numeroDocumento, String nombres, String apellidos, String email, String password, String rol) {
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException("Número de documento es obligatorio para crear usuario.");
        }

        String doc = numeroDocumento.trim();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNumeroDocumento(doc);

        Usuario usuario;
        if (usuarioOpt.isPresent()) {
            usuario = usuarioOpt.get();
            if (email != null && !email.isBlank()) usuario.setEmail(email.trim());
            if (password != null && !password.isBlank()) usuario.setPassword(password);
            if (rol != null && !rol.isBlank()) usuario.setRol(rol.trim());
        } else {
            String targetEmail = (email != null && !email.isBlank())
                    ? email.trim()
                    : generarEmailSugerido(nombres, apellidos);

            usuario = new Usuario(targetEmail, password, rol != null ? rol : "DOCENTE", doc);
        }

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> obtenerUsuarioAcceso(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.isBlank()) return Optional.empty();
        return usuarioRepository.findByNumeroDocumento(numeroDocumento);
    }

    private String slugify(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                         .replaceAll("[^a-z0-9]", "");
    }
}
