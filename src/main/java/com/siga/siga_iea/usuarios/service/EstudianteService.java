package com.siga.siga_iea.usuarios.service;

import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.repository.EstudianteRepository;
import com.siga.siga_iea.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;

    public EstudianteService(EstudianteRepository estudianteRepository, UsuarioRepository usuarioRepository) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAll();
    }

    public List<Estudiante> buscar(String search, String estado) {
        return estudianteRepository.searchEstudiantes(search, estado);
    }

    public Optional<Estudiante> buscarPorId(UUID id) {
        return estudianteRepository.findById(id);
    }

    public Optional<Estudiante> buscarPorNumeroDocumento(String doc) {
        return estudianteRepository.findByNumeroDocumento(doc);
    }

    @Transactional
    public Estudiante guardar(Estudiante estudiante) {
        if (estudiante.getCodigo() == null || estudiante.getCodigo().isBlank()) {
            estudiante.setCodigo(generarCodigoEstudiante());
        }
        return estudianteRepository.save(estudiante);
    }

    /**
     * Auto-generates a clean default institutional email based on names and surnames:
     * Example: "Santiago Alejandro" + "Gómez Pérez" -> "santiago.gomez@ieaci.edu.co"
     */
    public String generarEmailSugerido(String nombres, String apellidos) {
        String primerNombre = (nombres != null && !nombres.isBlank()) ? nombres.trim().split("\\s+")[0] : "estudiante";
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
    public Usuario crearOCambiarCuentaAcceso(UUID estudianteId, String email, String password) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado con id: " + estudianteId));

        if (estudiante.getNumeroDocumento() == null || estudiante.getNumeroDocumento().isBlank()) {
            throw new IllegalStateException("El estudiante debe tener un número de documento para asignarle usuario.");
        }

        String doc = estudiante.getNumeroDocumento();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNumeroDocumento(doc);

        Usuario usuario;
        if (usuarioOpt.isPresent()) {
            usuario = usuarioOpt.get();
            if (email != null && !email.isBlank()) usuario.setEmail(email.trim());
            if (password != null && !password.isBlank()) usuario.setPassword(password);
        } else {
            String targetEmail = (email != null && !email.isBlank())
                    ? email.trim()
                    : generarEmailSugerido(estudiante.getNombres(), estudiante.getApellidos());

            usuario = new Usuario(targetEmail, password, "ESTUDIANTE", doc);
        }

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> obtenerUsuarioAcceso(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.isBlank()) return Optional.empty();
        return usuarioRepository.findByNumeroDocumento(numeroDocumento);
    }

    private String generarCodigoEstudiante() {
        int year = java.time.Year.now().getValue();
        long count = estudianteRepository.count() + 1;
        return String.format("%d%03d", year, count);
    }

    private String slugify(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                         .replaceAll("[^a-z0-9]", "");
    }
}
