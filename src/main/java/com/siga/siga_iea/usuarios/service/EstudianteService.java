package com.siga.siga_iea.usuarios.service;

import com.siga.siga_iea.certificados.entity.SolicitudCertificado;
import com.siga.siga_iea.certificados.repository.SolicitudCertificadoRepository;
import com.siga.siga_iea.clases.entity.CursoEstudiante;
import com.siga.siga_iea.clases.repository.CursoEstudianteRepository;
import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;
import com.siga.siga_iea.reportes.entity.Reporte;
import com.siga.siga_iea.reportes.repository.ReporteRepository;
import com.siga.siga_iea.storage.entity.Documento;
import com.siga.siga_iea.storage.repository.DocumentoRepository;
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
    private final MatriculaRepository matriculaRepository;
    private final DocumentoRepository documentoRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;
    private final ReporteRepository reporteRepository;
    private final SolicitudCertificadoRepository certificadoRepository;

    public EstudianteService(EstudianteRepository estudianteRepository,
                            UsuarioRepository usuarioRepository,
                            MatriculaRepository matriculaRepository,
                            DocumentoRepository documentoRepository,
                            CursoEstudianteRepository cursoEstudianteRepository,
                            ReporteRepository reporteRepository,
                            SolicitudCertificadoRepository certificadoRepository) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.matriculaRepository = matriculaRepository;
        this.documentoRepository = documentoRepository;
        this.cursoEstudianteRepository = cursoEstudianteRepository;
        this.reporteRepository = reporteRepository;
        this.certificadoRepository = certificadoRepository;
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
        if (estudiante.getNombres() == null || estudiante.getNombres().isBlank()) {
            throw new IllegalArgumentException("El nombre del estudiante no puede estar vacío.");
        }
        if (estudiante.getNumeroDocumento() == null || estudiante.getNumeroDocumento().isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio.");
        }

        if (estudiante.getCodigo() == null || estudiante.getCodigo().isBlank()) {
            estudiante.setCodigo(generarCodigoEstudiante());
        }
        return estudianteRepository.save(estudiante);
    }

    @Transactional
    public void eliminarEstudiante(UUID estudianteId) {
        Estudiante e = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado con id: " + estudianteId));

        // 1. Eliminar cuenta de usuario si existe
        if (e.getNumeroDocumento() != null && !e.getNumeroDocumento().isBlank()) {
            usuarioRepository.findByNumeroDocumento(e.getNumeroDocumento())
                    .ifPresent(usuarioRepository::delete);
        }

        // 2. Eliminar inscripciones en cursos (CursoEstudiante)
        List<CursoEstudiante> cursos = cursoEstudianteRepository.findByEstudianteId(estudianteId);
        cursoEstudianteRepository.deleteAll(cursos);

        // 3. Eliminar reportes disciplinarios/académicos del estudiante
        List<Reporte> reportes = reporteRepository.findByEstudianteIdOrderByCreatedAtDesc(estudianteId);
        reporteRepository.deleteAll(reportes);

        // 4. Eliminar solicitudes de certificados del estudiante
        List<SolicitudCertificado> certs = certificadoRepository.findByEstudianteIdOrderByCreatedAtDesc(estudianteId);
        certificadoRepository.deleteAll(certs);

        // 5. Eliminar documentos asociados directamente al estudiante
        List<Documento> docsEstudiante = documentoRepository.findByEstudianteId(estudianteId);
        documentoRepository.deleteAll(docsEstudiante);

        // 6. Eliminar documentos asociados a las matrículas y eliminar matrículas
        List<Matricula> matriculas = matriculaRepository.findByEstudianteId(estudianteId);
        for (Matricula m : matriculas) {
            List<Documento> docsMatricula = documentoRepository.findByMatriculaId(m.getId());
            documentoRepository.deleteAll(docsMatricula);
        }
        matriculaRepository.deleteAll(matriculas);

        // 7. Eliminar la entidad Estudiante
        estudianteRepository.delete(e);
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
