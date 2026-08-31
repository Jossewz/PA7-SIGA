package com.siga.siga_iea.personal.controller;

import com.siga.siga_iea.storage.StorageFolder;
import com.siga.siga_iea.storage.StorageService;
import com.siga.siga_iea.storage.dto.UploadResult;
import com.siga.siga_iea.usuarios.entity.Docente;
import com.siga.siga_iea.usuarios.entity.PersonalAdministrativo;
import com.siga.siga_iea.usuarios.entity.Usuario;
import com.siga.siga_iea.usuarios.service.PersonalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class PersonalController {

    private final PersonalService personalService;
    private final StorageService storageService;

    public PersonalController(PersonalService personalService, StorageService storageService) {
        this.personalService = personalService;
        this.storageService = storageService;
    }

    @GetMapping("/personal")
    public String index(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "cargo", required = false) String cargo,
            @RequestParam(value = "area", required = false) String area,
            @RequestParam(value = "estado", required = false) String estado,
            Model model) {

        model.addAttribute("title", "Gestión de Personal – IEACI");
        model.addAttribute("activePage", "personal");
        model.addAttribute("search", search);
        model.addAttribute("cargo", cargo);
        model.addAttribute("area", area);
        model.addAttribute("estado", estado);

        List<Map<String, Object>> personalList = new ArrayList<>();

        // Fetch Docentes
        List<Docente> docentes = personalService.buscarDocentes(search, estado);
        for (Docente d : docentes) {
            if (cargo != null && !cargo.isBlank() && !"Docente".equalsIgnoreCase(cargo)) continue;
            if (area != null && !area.isBlank() && !"Académica".equalsIgnoreCase(area)) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId().toString());
            map.put("tipo", "DOCENTE");
            map.put("tipoPersona", "docente");
            map.put("nombres", d.getNombres() != null ? d.getNombres() : "Docente");
            map.put("apellidos", d.getApellidos() != null ? d.getApellidos() : "");
            map.put("nombreCompleto", d.getNombreCompleto());
            map.put("cargo", "Docente");
            map.put("area", "Académica");
            map.put("estado", d.getEstado() != null ? d.getEstado() : "Activo");
            map.put("tipoDocumento", d.getTipoDocumento() != null ? d.getTipoDocumento() : "CC");
            map.put("numeroDocumento", d.getNumeroDocumento());
            map.put("telefono", d.getTelefono() != null ? d.getTelefono() : "3001234567");
            map.put("foto", d.getFotoKey() != null ? "/storage/public/view?key=" + d.getFotoKey() : null);

            Optional<Usuario> usrOpt = personalService.obtenerUsuarioAcceso(d.getNumeroDocumento());
            map.put("tieneCuenta", usrOpt.isPresent());
            map.put("email", usrOpt.map(Usuario::getEmail).orElse("jrojas@ieaci.edu.co"));
            map.put("correo", usrOpt.map(Usuario::getEmail).orElse("jrojas@ieaci.edu.co"));
            map.put("usuarioEstado", usrOpt.map(Usuario::getEstado).orElse(null));

            personalList.add(map);
        }

        // Fetch PersonalAdministrativo
        List<PersonalAdministrativo> personalAdmin = personalService.buscarPersonal(search, cargo, area, estado);
        for (PersonalAdministrativo p : personalAdmin) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId().toString());
            map.put("tipo", "ADMINISTRATIVO");
            map.put("tipoPersona", "personal");
            map.put("nombres", p.getNombres() != null ? p.getNombres() : "Personal");
            map.put("apellidos", p.getApellidos() != null ? p.getApellidos() : "");
            map.put("nombreCompleto", p.getNombreCompleto());
            map.put("cargo", p.getCargo() != null ? p.getCargo() : "Administrativo");
            map.put("area", p.getArea() != null ? p.getArea() : "Administrativa");
            map.put("estado", p.getEstado() != null ? p.getEstado() : "Activo");
            map.put("tipoDocumento", p.getTipoDocumento() != null ? p.getTipoDocumento() : "CC");
            map.put("numeroDocumento", p.getNumeroDocumento());
            map.put("telefono", p.getTelefono() != null ? p.getTelefono() : "3001234567");
            map.put("foto", p.getFotoKey() != null ? "/storage/public/view?key=" + p.getFotoKey() : null);

            Optional<Usuario> usrOpt = personalService.obtenerUsuarioAcceso(p.getNumeroDocumento());
            map.put("tieneCuenta", usrOpt.isPresent());
            map.put("email", usrOpt.map(Usuario::getEmail).orElse("admin@ieaci.edu.co"));
            map.put("correo", usrOpt.map(Usuario::getEmail).orElse("admin@ieaci.edu.co"));
            map.put("usuarioEstado", usrOpt.map(Usuario::getEstado).orElse(null));

            personalList.add(map);
        }

        model.addAttribute("personalList", personalList);
        return "personal/index";
    }

    @PostMapping({"/personal", "/personal/registrar"})
    public String registrarPersonal(
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "nombres", required = false) String nombres,
            @RequestParam(value = "apellidos", required = false) String apellidos,
            @RequestParam(value = "tipoDocumento", required = false) String tipoDocumento,
            @RequestParam(value = "numeroDocumento", required = false) String numeroDocumento,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "direccion", required = false) String direccion,
            @RequestParam(value = "cargo", required = false) String cargo,
            @RequestParam(value = "area", required = false) String area,
            @RequestParam(value = "estudios", required = false) String estudios,
            @RequestParam(value = "profesion", required = false) String profesion,
            @RequestParam(value = "especialidad", required = false) String especialidad,
            @RequestParam(value = "sexo", required = false) String sexo,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            @RequestParam(value = "createUser", required = false, defaultValue = "false") boolean createUser,
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestParam(value = "passwordTemp", required = false) String passwordTemp,
            @RequestParam(value = "role", required = false) String role,

            @RequestParam(value = "primerNombre", required = false) String primerNombre,
            @RequestParam(value = "segundoNombre", required = false) String segundoNombre,
            @RequestParam(value = "primerApellido", required = false) String primerApellido,
            @RequestParam(value = "segundoApellido", required = false) String segundoApellido,
            @RequestParam(value = "tipoDoc", required = false) String tipoDoc,
            @RequestParam(value = "numDoc", required = false) String numDoc,
            RedirectAttributes redirectAttributes) {

        try {
            String nombresFull = (nombres != null && !nombres.isBlank()) ? nombres.trim() :
                    ((primerNombre != null ? primerNombre : "") + (segundoNombre != null && !segundoNombre.isBlank() ? " " + segundoNombre : "")).trim();

            String apellidosFull = (apellidos != null && !apellidos.isBlank()) ? apellidos.trim() :
                    ((primerApellido != null ? primerApellido : "") + (segundoApellido != null && !segundoApellido.isBlank() ? " " + segundoApellido : "")).trim();

            String docType = (tipoDocumento != null && !tipoDocumento.isBlank()) ? tipoDocumento.trim() : (tipoDoc != null ? tipoDoc.trim() : "CC");
            String docNum = (numeroDocumento != null && !numeroDocumento.isBlank()) ? numeroDocumento.trim() : (numDoc != null ? numDoc.trim() : "");

            if (nombresFull.isBlank() || apellidosFull.isBlank() || docNum.isBlank()) {
                throw new IllegalArgumentException("Nombres, apellidos y número de documento son obligatorios.");
            }

            String uploadedFotoKey = null;
            if (fotoFile != null && !fotoFile.isEmpty()) {
                storageService.validateSize(fotoFile, 10 * 1024 * 1024);
                storageService.validateExtension(fotoFile, "jpg", "jpeg", "png");
                UploadResult result = storageService.upload(fotoFile, StorageFolder.DOCENTES, docNum);
                uploadedFotoKey = result.getKey();
            }

            boolean esDocente = "DOCENTE".equalsIgnoreCase(tipo) || "Docente".equalsIgnoreCase(cargo);

            if (esDocente) {
                Docente d = new Docente();
                d.setNombres(nombresFull);
                d.setApellidos(apellidosFull);
                d.setTipoDocumento(docType);
                d.setNumeroDocumento(docNum);
                d.setGenero(sexo);
                d.setTelefono(telefono);
                d.setDireccion(direccion);
                d.setTitulo((estudios != null && !estudios.isBlank()) ? estudios : profesion);
                d.setEspecialidad(especialidad);
                d.setFotoKey(uploadedFotoKey);
                d.setEstado("Activo");
                personalService.guardarDocente(d);
            } else {
                PersonalAdministrativo p = new PersonalAdministrativo();
                p.setNombres(nombresFull);
                p.setApellidos(apellidosFull);
                p.setTipoDocumento(docType);
                p.setNumeroDocumento(docNum);
                p.setCargo((cargo != null && !cargo.isBlank()) ? cargo : ("DIRECTIVO".equalsIgnoreCase(tipo) ? "Directivo" : "Administrativo"));
                p.setArea((area != null && !area.isBlank()) ? area : ("DIRECTIVO".equalsIgnoreCase(tipo) ? "Directiva" : "Administrativa"));
                p.setGenero(sexo);
                p.setTelefono(telefono);
                p.setDireccion(direccion);
                p.setFotoKey(uploadedFotoKey);
                p.setEstado("Activo");
                personalService.guardarPersonal(p);
            }

            String targetEmail = (email != null && !email.isBlank()) ? email.trim() : userEmail;
            if (createUser || (targetEmail != null && !targetEmail.isBlank())) {
                String pass = (passwordTemp != null && !passwordTemp.isBlank()) ? passwordTemp : "IEACI" + java.time.Year.now().getValue() + "*";
                String targetRole = (role != null && !role.isBlank()) ? role : (esDocente ? "DOCENTE" : "PERSONAL_ADMINISTRATIVO");
                personalService.crearOCambiarCuentaAcceso(docNum, nombresFull, apellidosFull, targetEmail, pass, targetRole);
            }

            redirectAttributes.addFlashAttribute("mensajeExito", "Personal registrado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar personal: " + ex.getMessage());
        }

        return "redirect:/personal";
    }

    @GetMapping("/personal/perfil/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String perfil(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("title", "Perfil de Personal – IEACI");
        model.addAttribute("activePage", "personal");

        Optional<Docente> docenteOpt = Optional.empty();
        Optional<PersonalAdministrativo> adminOpt = Optional.empty();

        try {
            UUID uuid = UUID.fromString(id);
            docenteOpt = personalService.buscarDocentePorId(uuid);
            if (docenteOpt.isEmpty()) {
                adminOpt = personalService.buscarPersonalPorId(uuid);
            }
        } catch (IllegalArgumentException ignored) {}

        Map<String, Object> staff = new HashMap<>();

        if (docenteOpt.isPresent()) {
            Docente d = docenteOpt.get();
            staff.put("id", d.getId().toString());
            staff.put("tipoPersona", "docente");
            staff.put("primerNombre", d.getNombres());
            staff.put("primerApellido", d.getApellidos());
            staff.put("nombreCompleto", d.getNombreCompleto());
            staff.put("cargo", "Docente");
            staff.put("area", "Académica");
            staff.put("tipoDoc", d.getTipoDocumento() != null ? d.getTipoDocumento() : "CC");
            staff.put("numDoc", d.getNumeroDocumento() != null ? d.getNumeroDocumento() : "Sin documento");
            staff.put("numeroDocumento", d.getNumeroDocumento());
            staff.put("telefono", d.getTelefono() != null ? d.getTelefono() : "N/A");
            staff.put("direccion", d.getDireccion() != null ? d.getDireccion() : "N/A");
            staff.put("profesion", d.getTitulo() != null ? d.getTitulo() : "Licenciado");
            staff.put("especialidad", d.getEspecialidad() != null ? d.getEspecialidad() : "N/A");
            staff.put("estado", d.getEstado() != null ? d.getEstado() : "Activo");
            staff.put("foto", d.getFotoKey() != null ? "/storage/public/view?key=" + d.getFotoKey() : null);

            Optional<Usuario> usrOpt = personalService.obtenerUsuarioAcceso(d.getNumeroDocumento());
            if (usrOpt.isPresent()) {
                Usuario u = usrOpt.get();
                staff.put("tieneCuenta", true);
                staff.put("correo", u.getEmail());
                staff.put("usuarioRol", u.getRol() != null ? u.getRol() : "DOCENTE");
                staff.put("usuarioEstado", u.getEstado() != null ? u.getEstado() : "Activo");
                staff.put("emailSugerido", u.getEmail());
            } else {
                staff.put("tieneCuenta", false);
                staff.put("correo", "Sin cuenta creada");
                staff.put("usuarioRol", "DOCENTE");
                staff.put("usuarioEstado", "Inactivo");
                staff.put("emailSugerido", personalService.generarEmailSugerido(d.getNombres(), d.getApellidos()));
            }
        } else if (adminOpt.isPresent()) {
            PersonalAdministrativo p = adminOpt.get();

            staff.put("id", p.getId().toString());
            staff.put("tipoPersona", "personal");
            staff.put("primerNombre", p.getNombres());
            staff.put("primerApellido", p.getApellidos());
            staff.put("nombreCompleto", p.getNombreCompleto());
            staff.put("cargo", p.getCargo() != null ? p.getCargo() : "Administrativo");
            staff.put("area", p.getArea() != null ? p.getArea() : "Administrativa");
            staff.put("tipoDoc", p.getTipoDocumento() != null ? p.getTipoDocumento() : "CC");
            staff.put("numDoc", p.getNumeroDocumento() != null ? p.getNumeroDocumento() : "Sin documento");
            staff.put("numeroDocumento", p.getNumeroDocumento());
            staff.put("telefono", p.getTelefono() != null ? p.getTelefono() : "N/A");
            staff.put("direccion", p.getDireccion() != null ? p.getDireccion() : "N/A");
            staff.put("profesion", "Administrativo");
            staff.put("especialidad", "N/A");
            staff.put("estado", p.getEstado() != null ? p.getEstado() : "Activo");
            staff.put("foto", p.getFotoKey() != null ? "/storage/public/view?key=" + p.getFotoKey() : null);

            Optional<Usuario> usrOpt = personalService.obtenerUsuarioAcceso(p.getNumeroDocumento());
            if (usrOpt.isPresent()) {
                Usuario u = usrOpt.get();
                staff.put("tieneCuenta", true);
                staff.put("correo", u.getEmail());
                staff.put("usuarioRol", u.getRol() != null ? u.getRol() : "PERSONAL_ADMINISTRATIVO");
                staff.put("usuarioEstado", u.getEstado() != null ? u.getEstado() : "Activo");
                staff.put("emailSugerido", u.getEmail());
            } else {
                staff.put("tieneCuenta", false);
                staff.put("correo", "Sin cuenta creada");
                staff.put("usuarioRol", "PERSONAL_ADMINISTRATIVO");
                staff.put("usuarioEstado", "Inactivo");
                staff.put("emailSugerido", personalService.generarEmailSugerido(p.getNombres(), p.getApellidos()));
            }
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Personal no encontrado.");
            return "redirect:/personal";
        }

        model.addAttribute("staff", staff);
        model.addAttribute("activeTab", "informacion");
        return "personal/perfil";
    }

    @PostMapping("/personal/{id}/crear-cuenta")
    public String crearCuentaPersonal(
            @PathVariable String id,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "rol", required = false) String rol,
            RedirectAttributes redirectAttributes) {

        try {
            UUID uuid = UUID.fromString(id);
            String numDoc = null;
            String nombres = "";
            String apellidos = "";

            Optional<Docente> docOpt = personalService.buscarDocentePorId(uuid);
            if (docOpt.isPresent()) {
                numDoc = docOpt.get().getNumeroDocumento();
                nombres = docOpt.get().getNombres();
                apellidos = docOpt.get().getApellidos();
                if (rol == null || rol.isBlank()) rol = "DOCENTE";
            } else {
                PersonalAdministrativo p = personalService.buscarPersonalPorId(uuid)
                        .orElseThrow(() -> new IllegalArgumentException("Personal no encontrado"));
                numDoc = p.getNumeroDocumento();
                nombres = p.getNombres();
                apellidos = p.getApellidos();
                if (rol == null || rol.isBlank()) rol = "ADMIN";
            }

            String pass = (password != null && !password.isBlank()) ? password : "IEACI" + java.time.Year.now().getValue() + "*";
            Usuario u = personalService.crearOCambiarCuentaAcceso(numDoc, nombres, apellidos, email, pass, rol);

            redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta de usuario generada: " + u.getEmail());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear cuenta: " + ex.getMessage());
        }

        return "redirect:/personal/perfil/" + id;
    }
}
