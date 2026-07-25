package com.siga.siga_iea.matricula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import com.siga.siga_iea.storage.service.DocumentoService;

@Controller
public class MatriculaController {

    private final DocumentoService documentoService;

    public MatriculaController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping("/matricula")
    public String index(Model model, HttpSession session) {

        if (session.getAttribute("sede") == null)
            session.setAttribute("sede", "Sede Principal");
        if (session.getAttribute("grado") == null)
            session.setAttribute("grado", "9°");
        if (session.getAttribute("jornada") == null)
            session.setAttribute("jornada", "Mañana");

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
        model.addAttribute("parentName", session.getAttribute("parentNames")); // fallback
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

        String fotoKey = (String) session.getAttribute("fotoFileKey");
        if (fotoKey != null) {
            model.addAttribute("fotoFileUrl", "/storage/public/view?key=" + fotoKey);
        }

        model.addAttribute("currentStep", 1);
        model.addAttribute("title", "Formulario de Matrícula – IEACI");
        return "matricula/index";
    }

}
