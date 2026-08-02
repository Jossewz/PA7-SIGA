package com.siga.siga_iea.estudiantes.controller;

import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.repository.MatriculaRepository;
import com.siga.siga_iea.storage.entity.Documento;
import com.siga.siga_iea.storage.entity.TipoDocumento;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import com.siga.siga_iea.usuarios.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final MatriculaRepository matriculaRepository;
    private final UsuarioService usuarioService;

    public EstudianteController(EstudianteService estudianteService,
                                MatriculaRepository matriculaRepository,
                                UsuarioService usuarioService) {
        this.estudianteService = estudianteService;
        this.matriculaRepository = matriculaRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/estudiantes")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String index(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "estado", required = false) String estado,
            Model model) {

        model.addAttribute("title", "Gestión de Estudiantes – IEACI");
        model.addAttribute("activePage", "estudiantes");
        model.addAttribute("search", search);
        model.addAttribute("estado", estado);

        List<Estudiante> dbEstudiantes = estudianteService.buscar(search, estado);

        List<Map<String, Object>> estudiantesList = new ArrayList<>();

        if (!dbEstudiantes.isEmpty()) {
            for (Estudiante e : dbEstudiantes) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", e.getId().toString());
                map.put("codigo", e.getCodigo() != null ? e.getCodigo() : "N/A");
                map.put("nombreCompleto", e.getNombreCompleto());
                map.put("numeroDocumento", e.getNumeroDocumento());
                map.put("estado", e.getEstado() != null ? e.getEstado() : "Activo");

                Optional<Matricula> matOpt = matriculaRepository.findTopByEstudianteIdOrderByFechaMatriculaDesc(e.getId());
                String grad = matOpt.map(m -> m.getGrado() != null ? m.getGrado() : "11°").orElse("11°");
                String sal = matOpt.map(m -> m.getSalon() != null ? m.getSalon() : "01").orElse("01");

                map.put("grado", grad);
                map.put("salon", sal);
                map.put("curso", grad + " - " + sal);
                map.put("foto", resolverFotoUrl(e, matOpt));

                Optional<Usuario> usrOpt = estudianteService.obtenerUsuarioAcceso(e.getNumeroDocumento());
                map.put("tieneCuenta", usrOpt.isPresent());
                map.put("usuarioEmail", usrOpt.map(Usuario::getEmail).orElse(null));
                map.put("usuarioEstado", usrOpt.map(Usuario::getEstado).orElse(null));

                estudiantesList.add(map);
            }
        }

        model.addAttribute("estudiantesList", estudiantesList);
        return "estudiantes/index";
    }

    @GetMapping("/estudiantes/perfil/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String perfil(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("title", "Perfil del Estudiante – IEACI");
        model.addAttribute("activePage", "estudiantes");

        // First try finding in DB if id is UUID
        Optional<Estudiante> estOpt = Optional.empty();
        try {
            UUID uuid = UUID.fromString(id);
            estOpt = estudianteService.buscarPorId(uuid);
        } catch (IllegalArgumentException ignored) {}

        if (estOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Estudiante no encontrado.");
            return "redirect:/estudiantes";
        }

        Map<String, Object> student = new HashMap<>();
        Estudiante e = estOpt.get();
        student.put("id", e.getId().toString());
        student.put("codigo", e.getCodigo() != null ? e.getCodigo() : "Sin código");
        student.put("nombres", e.getNombres());
        student.put("apellidos", e.getApellidos());
        student.put("nombreCompleto", e.getNombreCompleto());
        student.put("tipoDocumento", e.getTipoDocumento() != null ? e.getTipoDocumento() : "TI");
        student.put("documento", (e.getTipoDocumento() != null ? e.getTipoDocumento() : "TI") + " - " + (e.getNumeroDocumento() != null ? e.getNumeroDocumento() : "Sin Doc"));
        student.put("numeroDocumento", e.getNumeroDocumento());
        student.put("genero", e.getGenero() != null ? e.getGenero() : "No especificado");
        student.put("telefono", e.getTelefono() != null ? e.getTelefono() : "No especificado");
        student.put("fechaNac", e.getFechaNacimiento() != null ? e.getFechaNacimiento().toString() : "No registrada");
        student.put("direccion", e.getDireccion() != null ? e.getDireccion() : "No registrada");
        student.put("estado", e.getEstado() != null ? e.getEstado() : "Activo");

        if (e.getAcudiente() != null) {
            student.put("acudienteNombre", e.getAcudiente().getNombreCompleto());
            student.put("acudienteParentesco", e.getAcudiente().getParentesco() != null ? e.getAcudiente().getParentesco() : "Acudiente");
            student.put("acudienteDoc", (e.getAcudiente().getTipoDocumento() != null ? e.getAcudiente().getTipoDocumento() : "CC") + " - " + (e.getAcudiente().getNumeroDocumento() != null ? e.getAcudiente().getNumeroDocumento() : "N/A"));
            student.put("acudienteTel", e.getAcudiente().getTelefono() != null ? e.getAcudiente().getTelefono() : "N/A");
            student.put("acudienteDir", e.getAcudiente().getDireccion() != null ? e.getAcudiente().getDireccion() : "N/A");
        } else {
            student.put("acudienteNombre", "No asignado");
            student.put("acudienteParentesco", "-");
            student.put("acudienteDoc", "-");
            student.put("acudienteTel", "-");
            student.put("acudienteDir", "-");
        }

        Optional<Matricula> matOpt = matriculaRepository.findTopByEstudianteIdOrderByFechaMatriculaDesc(e.getId());
        String grad = matOpt.map(m -> m.getGrado() != null ? m.getGrado() : "11°").orElse("11°");
        String sal = matOpt.map(m -> m.getSalon() != null ? m.getSalon() : "01").orElse("01");

        student.put("grado", grad);
        student.put("salon", sal);
        student.put("curso", grad + " - " + sal);
        student.put("anoLectivo", matOpt.map(Matricula::getAnoLectivo).orElse("-"));
        student.put("estadoMatricula", matOpt.map(Matricula::getEstado).orElse("SIN_MATRICULA"));
        student.put("foto", resolverFotoUrl(e, matOpt));

        Optional<Usuario> usrOpt = estudianteService.obtenerUsuarioAcceso(e.getNumeroDocumento());
        if (usrOpt.isPresent()) {
            Usuario u = usrOpt.get();
            student.put("tieneCuenta", true);
            student.put("usuarioId", u.getId().toString());
            student.put("usuarioEmail", u.getEmail());
            student.put("usuarioRol", u.getRol());
            student.put("usuarioEstado", u.getEstado());
        } else {
            student.put("tieneCuenta", false);
            student.put("emailSugerido", estudianteService.generarEmailSugerido(e.getNombres(), e.getApellidos()));
        }

        model.addAttribute("student", student);
        return "estudiantes/perfil";
    }

    @PostMapping("/estudiantes/{id}/crear-cuenta")
    public String crearCuentaAcceso(
            @PathVariable String id,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            RedirectAttributes redirectAttributes) {

        try {
            UUID uuid = UUID.fromString(id);
            String pass = (password != null && !password.isBlank()) ? password : "IEACI" + java.time.Year.now().getValue() + "*";
            Usuario usuario = estudianteService.crearOCambiarCuentaAcceso(uuid, email, pass);

            redirectAttributes.addFlashAttribute("mensajeExito", 
                    "Cuenta de usuario creada/actualizada exitosamente: " + usuario.getEmail());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al generar cuenta: " + ex.getMessage());
        }

        return "redirect:/estudiantes/perfil/" + id;
    }

    @PostMapping("/estudiantes/{id}/toggle-estado")
    public String toggleEstadoEstudiante(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            UUID uuid = UUID.fromString(id);
            Estudiante e = estudianteService.buscarPorId(uuid)
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

            e.setEstado("Activo".equalsIgnoreCase(e.getEstado()) ? "Retirado" : "Activo");
            estudianteService.guardar(e);

            redirectAttributes.addFlashAttribute("mensajeExito", "Estado del estudiante actualizado a " + e.getEstado());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar estado: " + ex.getMessage());
        }

        return "redirect:/estudiantes/perfil/" + id;
    }

    private String resolverFotoUrl(Estudiante e, Optional<Matricula> matOpt) {
        String key = e.getFotoKey();

        if (key == null || key.isBlank() || key.startsWith("temp/")) {
            if (matOpt.isPresent() && matOpt.get().getDocumentos() != null) {
                for (Documento doc : matOpt.get().getDocumentos()) {
                    if (doc.getTipoDocumento() == TipoDocumento.FOTO_ESTUDIANTE) {
                        key = doc.getStorageKey();
                        break;
                    }
                }
            }
        }

        if (key == null || key.isBlank()) return null;
        return key.startsWith("http") ? key : "/storage/public/view?key=" + key;
    }
}
