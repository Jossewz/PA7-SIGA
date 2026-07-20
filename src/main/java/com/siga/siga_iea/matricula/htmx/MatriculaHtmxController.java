package com.siga.siga_iea.matricula.htmx;

import com.siga.siga_iea.matricula.entity.Matricula;
import com.siga.siga_iea.matricula.service.MatriculaService;
import com.siga.siga_iea.storage.entity.TipoDocumento;
import com.siga.siga_iea.storage.service.DocumentoService;
import com.siga.siga_iea.usuarios.entity.Acudiente;
import com.siga.siga_iea.usuarios.entity.Estudiante;
import com.siga.siga_iea.usuarios.service.AcudienteService;
import com.siga.siga_iea.usuarios.service.EstudianteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/matricula")
public class MatriculaHtmxController {

    private final MatriculaService matriculaService;
    private final DocumentoService documentoService;
    private final EstudianteService estudianteService;
    private final AcudienteService acudienteService;

    public MatriculaHtmxController(MatriculaService matriculaService,
                                   DocumentoService documentoService,
                                   EstudianteService estudianteService,
                                   AcudienteService acudienteService) {
        this.matriculaService = matriculaService;
        this.documentoService = documentoService;
        this.estudianteService = estudianteService;
        this.acudienteService = acudienteService;
    }

    @PostMapping("/update-name")
    @ResponseBody
    public String updateName(
            @RequestParam(value = "studentNames", required = false) String names,
            @RequestParam(value = "studentSurnames", required = false) String surnames,
            HttpSession session) {
        if (names != null) session.setAttribute("studentNames", names.trim());
        if (surnames != null) session.setAttribute("studentSurnames", surnames.trim());

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
        if (sede != null) session.setAttribute("sede", sede);
        if (grado != null) session.setAttribute("grado", grado);
        if (jornada != null) session.setAttribute("jornada", jornada);

        populateModelFromSession(session, model);
        model.addAttribute("currentStep", 3);

        return "matricula/htmx-step";
    }

    @PostMapping("/paso/{step}")
    public String loadStep(
            @PathVariable("step") int step,
            @RequestParam(value = "studentNames", required = false) String studentNames,
            @RequestParam(value = "studentSurnames", required = false) String studentSurnames,
            @RequestParam(value = "studentDocType", required = false) String studentDocType,
            @RequestParam(value = "studentDocNumber", required = false) String studentDocNumber,
            @RequestParam(value = "studentGender", required = false) String studentGender,
            @RequestParam(value = "studentPhone", required = false) String studentPhone,
            @RequestParam(value = "studentBirthday", required = false) String studentBirthday,
            @RequestParam(value = "studentAddress", required = false) String studentAddress,
            @RequestParam(value = "parentNames", required = false) String parentNames,
            @RequestParam(value = "parentSurnames", required = false) String parentSurnames,
            @RequestParam(value = "parentDocType", required = false) String parentDocType,
            @RequestParam(value = "parentId", required = false) String parentId,
            @RequestParam(value = "parentRelation", required = false) String parentRelation,
            @RequestParam(value = "parentPhone", required = false) String parentPhone,
            @RequestParam(value = "parentAddress", required = false) String parentAddress,
            HttpSession session,
            Model model) {

        if (studentNames != null) session.setAttribute("studentNames", studentNames);
        if (studentSurnames != null) session.setAttribute("studentSurnames", studentSurnames);
        if (studentDocType != null) session.setAttribute("studentDocType", studentDocType);
        if (studentDocNumber != null) session.setAttribute("studentDocNumber", studentDocNumber);
        if (studentGender != null) session.setAttribute("studentGender", studentGender);
        if (studentPhone != null) session.setAttribute("studentPhone", studentPhone);
        if (studentBirthday != null) session.setAttribute("studentBirthday", studentBirthday);
        if (studentAddress != null) session.setAttribute("studentAddress", studentAddress);

        if (parentNames != null) session.setAttribute("parentNames", parentNames);
        if (parentSurnames != null) session.setAttribute("parentSurnames", parentSurnames);
        if (parentDocType != null) session.setAttribute("parentDocType", parentDocType);
        if (parentId != null) session.setAttribute("parentId", parentId);
        if (parentRelation != null) session.setAttribute("parentRelation", parentRelation);
        if (parentPhone != null) session.setAttribute("parentPhone", parentPhone);
        if (parentAddress != null) session.setAttribute("parentAddress", parentAddress);

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
            String[] extensions = fieldName.equals("fotoFile")
                    ? new String[]{"jpg", "jpeg", "png"}
                    : new String[]{"pdf", "jpg", "jpeg", "png"};

            String storageKey = documentoService.subirTemporal(file, session.getId(), extensions);
            session.setAttribute(fieldName + "Key", storageKey);
            session.setAttribute(fieldName + "Name", file.getOriginalFilename());
            session.setAttribute(fieldName + "ContentType", file.getContentType());
            session.setAttribute(fieldName + "Size", file.getSize());
        }
        populateModelFromSession(session, model);

        return switch (fieldName) {
            case "parentDoc" -> "matricula/fragments/step-acudiente :: parentDocResponse";
            case "civilDoc" -> "matricula/fragments/step-documentos :: civilDocResponse";
            case "saludFile" -> "matricula/fragments/step-documentos :: saludFileResponse";
            case "fotoFile" -> "matricula/fragments/step-documentos :: fotoFileResponse";
            case "historialFile" -> "matricula/fragments/step-documentos :: historialFileResponse";
            default -> "matricula/htmx-step";
        };
    }

    @PostMapping("/delete-file")
    public String deleteFile(
            @RequestParam("fieldName") String fieldName,
            HttpSession session,
            Model model) {
        String storageKey = (String) session.getAttribute(fieldName + "Key");
        if (storageKey != null) {
            documentoService.eliminarTemporal(storageKey);
        }
        session.removeAttribute(fieldName + "Key");
        session.removeAttribute(fieldName + "Name");
        session.removeAttribute(fieldName + "ContentType");
        session.removeAttribute(fieldName + "Size");
        populateModelFromSession(session, model);

        return switch (fieldName) {
            case "parentDoc" -> "matricula/fragments/step-acudiente :: parentDocResponse";
            case "civilDoc" -> "matricula/fragments/step-documentos :: civilDocResponse";
            case "saludFile" -> "matricula/fragments/step-documentos :: saludFileResponse";
            case "fotoFile" -> "matricula/fragments/step-documentos :: fotoFileResponse";
            case "historialFile" -> "matricula/fragments/step-documentos :: historialFileResponse";
            default -> "matricula/htmx-step";
        };
    }

    @PostMapping("/paso/finalizar")
    @Transactional
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

        // 1. Guardar/Buscar Acudiente
        String parentNames = (String) session.getAttribute("parentNames");
        String parentSurnames = (String) session.getAttribute("parentSurnames");
        String parentDocType = (String) session.getAttribute("parentDocType");
        String parentId = (String) session.getAttribute("parentId");
        String parentRelation = (String) session.getAttribute("parentRelation");
        String parentPhone = (String) session.getAttribute("parentPhone");
        String parentAddress = (String) session.getAttribute("parentAddress");

        Acudiente acudiente = acudienteService.buscarOCrear(
                parentNames, parentSurnames, parentRelation,
                parentDocType, parentId, parentPhone, parentAddress
        );

        // 2. Guardar Estudiante con sus datos reales
        Estudiante estudiante = new Estudiante();
        estudiante.setNombres((String) session.getAttribute("studentNames"));
        estudiante.setApellidos((String) session.getAttribute("studentSurnames"));
        estudiante.setTipoDocumento((String) session.getAttribute("studentDocType"));
        estudiante.setNumeroDocumento((String) session.getAttribute("studentDocNumber"));
        estudiante.setGenero((String) session.getAttribute("studentGender"));
        estudiante.setTelefono((String) session.getAttribute("studentPhone"));

        String birthdayStr = (String) session.getAttribute("studentBirthday");
        if (birthdayStr != null && !birthdayStr.isBlank()) {
            try {
                estudiante.setFechaNacimiento(LocalDate.parse(birthdayStr));
            } catch (Exception ignored) {}
        }

        estudiante.setDireccion((String) session.getAttribute("studentAddress"));
        estudiante.setFotoKey((String) session.getAttribute("fotoFileKey"));
        estudiante.setAcudiente(acudiente);
        estudiante.setEstado("Activo");

        estudiante = estudianteService.guardar(estudiante);

        // 3. Guardar Matrícula
        Matricula nuevaMatricula = new Matricula();
        nuevaMatricula.setEstudiante(estudiante);

        String grado = (String) session.getAttribute("grado");
        nuevaMatricula.setGrado(grado != null ? grado : "No Asignado");
        nuevaMatricula.setAnoLectivo(String.valueOf(Year.now().getValue()));
        nuevaMatricula.setEstado("PENDIENTE_DE_REVISION");
        nuevaMatricula.setFechaMatricula(LocalDate.now());

        matriculaService.guardar(nuevaMatricula);

        // 4. Vincular documentos a la matrícula
        Map<String, TipoDocumento> fieldToTipo = Map.of(
                "parentDoc", TipoDocumento.DOCUMENTO_ACUDIENTE,
                "civilDoc", TipoDocumento.REGISTRO_CIVIL,
                "saludFile", TipoDocumento.CERTIFICADO_SALUD,
                "fotoFile", TipoDocumento.FOTO_ESTUDIANTE,
                "historialFile", TipoDocumento.HISTORIAL_ACADEMICO
        );

        for (var entry : fieldToTipo.entrySet()) {
            String field = entry.getKey();
            String tempKey = (String) session.getAttribute(field + "Key");
            if (tempKey != null) {
                Documento doc = documentoService.vincularAMatricula(
                        tempKey,
                        nuevaMatricula,
                        entry.getValue(),
                        (String) session.getAttribute(field + "Name"),
                        (String) session.getAttribute(field + "ContentType"),
                        (Long) session.getAttribute(field + "Size")
                );
                if ("fotoFile".equals(field)) {
                    estudiante.setFotoKey(doc.getStorageKey());
                    estudianteService.guardar(estudiante);
                }
            }
        }

        // 5. Limpiar sesión
        List<String> keysToRemove = List.of(
                "studentNames", "studentSurnames", "studentDocType", "studentDocNumber",
                "studentGender", "studentPhone", "studentBirthday", "studentAddress",
                "parentNames", "parentSurnames", "parentDocType", "parentId", "parentRelation",
                "parentPhone", "parentAddress", "parentDocName", "civilDocName",
                "saludFileName", "fotoFileName", "historialFileName"
        );
        keysToRemove.forEach(session::removeAttribute);

        for (String field : List.of("parentDoc", "civilDoc", "saludFile", "fotoFile", "historialFile")) {
            session.removeAttribute(field + "Key");
            session.removeAttribute(field + "ContentType");
            session.removeAttribute(field + "Size");
        }

        return "matricula/fragments/success-step";
    }

    private void populateModelFromSession(HttpSession session, Model model) {
        model.addAttribute("studentNames", session.getAttribute("studentNames"));
        model.addAttribute("studentSurnames", session.getAttribute("studentSurnames"));
        model.addAttribute("studentDocType", session.getAttribute("studentDocType"));
        model.addAttribute("studentDocNumber", session.getAttribute("studentDocNumber"));
        model.addAttribute("studentGender", session.getAttribute("studentGender"));
        model.addAttribute("studentPhone", session.getAttribute("studentPhone"));
        model.addAttribute("studentBirthday", session.getAttribute("studentBirthday"));
        model.addAttribute("studentAddress", session.getAttribute("studentAddress"));

        model.addAttribute("parentNames", session.getAttribute("parentNames"));
        model.addAttribute("parentSurnames", session.getAttribute("parentSurnames"));
        model.addAttribute("parentDocType", session.getAttribute("parentDocType"));
        model.addAttribute("parentId", session.getAttribute("parentId"));
        model.addAttribute("parentRelation", session.getAttribute("parentRelation"));
        model.addAttribute("parentPhone", session.getAttribute("parentPhone"));
        model.addAttribute("parentAddress", session.getAttribute("parentAddress"));

        model.addAttribute("sede", session.getAttribute("sede"));
        model.addAttribute("grado", session.getAttribute("grado"));
        model.addAttribute("jornada", session.getAttribute("jornada"));

        model.addAttribute("parentDocName", session.getAttribute("parentDocName"));
        model.addAttribute("civilDocName", session.getAttribute("civilDocName"));
        model.addAttribute("saludFileName", session.getAttribute("saludFileName"));
        model.addAttribute("fotoFileName", session.getAttribute("fotoFileName"));
        model.addAttribute("historialFileName", session.getAttribute("historialFileName"));

        String fotoKey = (String) session.getAttribute("fotoFileKey");
        if (fotoKey != null) {
            model.addAttribute("fotoFileUrl", "/storage/public/view?key=" + fotoKey);
        }
    }
}
