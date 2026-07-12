package com.siga.siga_iea.matricula.htmx;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/matricula")
public class MatriculaHtmxController {

    private final com.siga.siga_iea.usuarios.service.UsuarioService usuarioService;
    private final com.siga.siga_iea.matricula.service.MatriculaService matriculaService;

    public MatriculaHtmxController(com.siga.siga_iea.usuarios.service.UsuarioService usuarioService,
            com.siga.siga_iea.matricula.service.MatriculaService matriculaService) {
        this.usuarioService = usuarioService;
        this.matriculaService = matriculaService;
    }

    @PostMapping("/update-name")
    @ResponseBody
    public String updateName(
            @RequestParam(value = "studentNames", required = false) String names,
            @RequestParam(value = "studentSurnames", required = false) String surnames,
            HttpSession session) {
        if (names != null)
            session.setAttribute("studentNames", names.trim());
        if (surnames != null)
            session.setAttribute("studentSurnames", surnames.trim());

        String fNames = (String) session.getAttribute("studentNames");
        String fSurnames = (String) session.getAttribute("studentSurnames");
        String fullName = ((fNames != null ? fNames : "") + " " + (fSurnames != null ? fSurnames : "")).trim();
        return fullName.isEmpty() ? "Julián Andrés Gómez" : fullName;
    }

    @PostMapping("/paso/3/selection")
    public String handleStep3Selection(
            @RequestParam(value = "sede", required = false) String sede,
            @RequestParam(value = "grado", required = false) String grado,
            @RequestParam(value = "jornada", required = false) String jornada,
            HttpSession session,
            Model model) {
        if (sede != null)
            session.setAttribute("sede", sede);
        if (grado != null)
            session.setAttribute("grado", grado);
        if (jornada != null)
            session.setAttribute("jornada", jornada);

        populateModelFromSession(session, model);
        model.addAttribute("currentStep", 3);

        return "matricula/htmx-step";
    }

    @PostMapping("/paso/{step}")
    public String loadStep(
            @PathVariable("step") int step,
            @RequestParam(value = "studentNames", required = false) String studentNames,
            @RequestParam(value = "studentSurnames", required = false) String studentSurnames,
            @RequestParam(value = "studentGender", required = false) String studentGender,
            @RequestParam(value = "studentPhone", required = false) String studentPhone,
            @RequestParam(value = "studentBirthday", required = false) String studentBirthday,
            @RequestParam(value = "studentAddress", required = false) String studentAddress,
            @RequestParam(value = "parentName", required = false) String parentName,
            @RequestParam(value = "parentRelation", required = false) String parentRelation,
            @RequestParam(value = "parentId", required = false) String parentId,
            @RequestParam(value = "parentPhone", required = false) String parentPhone,
            @RequestParam(value = "parentAddress", required = false) String parentAddress,
            HttpSession session,
            Model model) {
        // Save form parameters from previous steps into the session
        if (studentNames != null)
            session.setAttribute("studentNames", studentNames);
        if (studentSurnames != null)
            session.setAttribute("studentSurnames", studentSurnames);
        if (studentGender != null)
            session.setAttribute("studentGender", studentGender);
        if (studentPhone != null)
            session.setAttribute("studentPhone", studentPhone);
        if (studentBirthday != null)
            session.setAttribute("studentBirthday", studentBirthday);
        if (studentAddress != null)
            session.setAttribute("studentAddress", studentAddress);

        if (parentName != null)
            session.setAttribute("parentName", parentName);
        if (parentRelation != null)
            session.setAttribute("parentRelation", parentRelation);
        if (parentId != null)
            session.setAttribute("parentId", parentId);
        if (parentPhone != null)
            session.setAttribute("parentPhone", parentPhone);
        if (parentAddress != null)
            session.setAttribute("parentAddress", parentAddress);

        // Populate model
        populateModelFromSession(session, model);
        model.addAttribute("currentStep", step);

        return "matricula/htmx-step";
    }

    @PostMapping("/upload-file")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fieldName") String fieldName,
            HttpSession session,
            Model model) {
        if (!file.isEmpty()) {
            session.setAttribute(fieldName + "Name", file.getOriginalFilename());
        }
        populateModelFromSession(session, model);

        return switch (fieldName) {
            case "parentDoc" -> "matricula/fragments/step-acudiente :: parentDocContainer";
            case "civilDoc" -> "matricula/fragments/step-documentos :: civilDocContainer";
            case "saludFile" -> "matricula/fragments/step-documentos :: saludFileContainer";
            case "fotoFile" -> "matricula/fragments/step-documentos :: fotoFileContainer";
            case "historialFile" -> "matricula/fragments/step-documentos :: historialFileContainer";
            default -> "matricula/htmx-step";
        };
    }

    @PostMapping("/delete-file")
    public String deleteFile(
            @RequestParam("fieldName") String fieldName,
            HttpSession session,
            Model model) {
        session.removeAttribute(fieldName + "Name");
        populateModelFromSession(session, model);

        return switch (fieldName) {
            case "parentDoc" -> "matricula/fragments/step-acudiente :: parentDocContainer";
            case "civilDoc" -> "matricula/fragments/step-documentos :: civilDocContainer";
            case "saludFile" -> "matricula/fragments/step-documentos :: saludFileContainer";
            case "fotoFile" -> "matricula/fragments/step-documentos :: fotoFileContainer";
            case "historialFile" -> "matricula/fragments/step-documentos :: historialFileContainer";
            default -> "matricula/htmx-step";
        };
    }

    @PostMapping("/paso/finalizar")
    public String handleFinalize(HttpSession session, Model model) {
        String civil = (String) session.getAttribute("civilDocName");
        String salud = (String) session.getAttribute("saludFileName");
        String foto = (String) session.getAttribute("fotoFileName");
        String historial = (String) session.getAttribute("historialFileName");

        if (civil == null || salud == null || foto == null || historial == null) {
            model.addAttribute("error", "Por favor, cargue todos los documentos requeridos antes de finalizar.");
            model.addAttribute("currentStep", 4);
            populateModelFromSession(session, model);
            return "matricula/htmx-step";
        }

        // 1. Guardar Estudiante (Usuario) en la Base de Datos
        com.siga.siga_iea.usuarios.entity.Usuario estudiante = new com.siga.siga_iea.usuarios.entity.Usuario();
        String nombres = (String) session.getAttribute("studentNames");
        String apellidos = (String) session.getAttribute("studentSurnames");

        estudiante.setNombres(nombres != null ? nombres : "Estudiante Sin Nombre");
        estudiante.setApellidos(apellidos != null ? apellidos : "");
        estudiante.setRol("ESTUDIANTE");

        // Generamos un correo dummy único ya que la vista aún no pide email pero la BD
        // lo exige
        estudiante.setEmail("estudiante_" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@ieaci.edu.co");

        // Guardamos el acudiente/documentos temporalmente en los campos libres o
        // creamos otras entidades a futuro
        estudiante.setTipoDocumento("TI");

        usuarioService.guardar(estudiante);

        // 2. Guardar Matrícula en la Base de Datos
        com.siga.siga_iea.matricula.entity.Matricula nuevaMatricula = new com.siga.siga_iea.matricula.entity.Matricula();
        nuevaMatricula.setEstudiante(estudiante);

        String grado = (String) session.getAttribute("grado");
        nuevaMatricula.setGrado(grado != null ? grado : "No Asignado");

        nuevaMatricula.setAnoLectivo(String.valueOf(java.time.Year.now().getValue()));
        nuevaMatricula.setEstado("PENDIENTE_DE_REVISION");
        nuevaMatricula.setFechaMatricula(java.time.LocalDate.now());

        matriculaService.guardar(nuevaMatricula);

        // 3. Clear all form attributes from session after completion

        session.removeAttribute("studentNames");
        session.removeAttribute("studentSurnames");
        session.removeAttribute("studentGender");
        session.removeAttribute("studentPhone");
        session.removeAttribute("studentBirthday");
        session.removeAttribute("studentAddress");
        session.removeAttribute("parentName");
        session.removeAttribute("parentRelation");
        session.removeAttribute("parentId");
        session.removeAttribute("parentPhone");
        session.removeAttribute("parentAddress");
        session.removeAttribute("parentDocName");
        session.removeAttribute("civilDocName");
        session.removeAttribute("saludFileName");
        session.removeAttribute("fotoFileName");
        session.removeAttribute("historialFileName");

        return "matricula/fragments/success-step";
    }

    private void populateModelFromSession(HttpSession session, Model model) {
        model.addAttribute("studentNames", session.getAttribute("studentNames"));
        model.addAttribute("studentSurnames", session.getAttribute("studentSurnames"));
        model.addAttribute("studentGender", session.getAttribute("studentGender"));
        model.addAttribute("studentPhone", session.getAttribute("studentPhone"));
        model.addAttribute("studentBirthday", session.getAttribute("studentBirthday"));
        model.addAttribute("studentAddress", session.getAttribute("studentAddress"));

        model.addAttribute("parentName", session.getAttribute("parentName"));
        model.addAttribute("parentRelation", session.getAttribute("parentRelation"));
        model.addAttribute("parentId", session.getAttribute("parentId"));
        model.addAttribute("parentPhone", session.getAttribute("parentPhone"));
        model.addAttribute("parentAddress", session.getAttribute("parentAddress"));

        model.addAttribute("sede", session.getAttribute("sede"));
        model.addAttribute("grado", session.getAttribute("grado"));
        model.addAttribute("jornada", session.getAttribute("jornada"));

        // File names
        model.addAttribute("parentDocName", session.getAttribute("parentDocName"));
        model.addAttribute("civilDocName", session.getAttribute("civilDocName"));
        model.addAttribute("saludFileName", session.getAttribute("saludFileName"));
        model.addAttribute("fotoFileName", session.getAttribute("fotoFileName"));
        model.addAttribute("historialFileName", session.getAttribute("historialFileName"));
    }
}
