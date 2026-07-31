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

        // Fallback sample items if DB is empty
        if (personalList.isEmpty()) {
            personalList.add(createSampleStaff("1", "Carlos", "Mendoza", "1088123456", "Docente", "DOCENTE", "carlos.mendoza@ieaci.edu.co", "Activo", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&h=120&q=80"));
            personalList.add(createSampleStaff("2", "Ana María", "Sánchez", "1045321456", "Coordinador", "ADMINISTRATIVO", "ana.sanchez@ieaci.edu.co", "Activo", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&h=120&q=80"));
            personalList.add(createSampleStaff("3", "Luis Felipe", "Gómez", "1073123987", "Secretario", "ADMINISTRATIVO", "luis.gomez@ieaci.edu.co", "Activo", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=120&h=120&q=80"));
            personalList.add(createSampleStaff("4", "Patricia", "López", "1045987123", "Bibliotecario", "ADMINISTRATIVO", "patricia.lopez@ieaci.edu.co", "Inactivo", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=120&h=120&q=80"));
            personalList.add(createSampleStaff("5", "Jorge Eliécer", "Rojas", "1012345678", "Rector", "DOCENTE", "jorge.rojas@ieaci.edu.co", "Activo", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=120&h=120&q=80"));
        }

        model.addAttribute("personalList", personalList);
        return "personal/index";
    }

    @PostMapping("/personal/registrar")
    public String registrarPersonal(
            @RequestParam("primerNombre") String primerNombre,
            @RequestParam(value = "segundoNombre", required = false) String segundoNombre,
            @RequestParam("primerApellido") String primerApellido,
            @RequestParam(value = "segundoApellido", required = false) String segundoApellido,
            @RequestParam("tipoDoc") String tipoDoc,
            @RequestParam("numDoc") String numDoc,
            @RequestParam(value = "sexo", required = false) String sexo,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "direccion", required = false) String direccion,
            @RequestParam("cargo") String cargo,
            @RequestParam(value = "area", required = false) String area,
            @RequestParam(value = "profesion", required = false) String profesion,
            @RequestParam(value = "especialidad", required = false) String especialidad,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            @RequestParam(value = "createUser", required = false, defaultValue = "false") boolean createUser,
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestParam(value = "passwordTemp", required = false) String passwordTemp,
            @RequestParam(value = "role", required = false) String role,
            RedirectAttributes redirectAttributes) {

        try {
            String nombresFull = (primerNombre + (segundoNombre != null && !segundoNombre.isBlank() ? " " + segundoNombre : "")).trim();
            String apellidosFull = (primerApellido + (segundoApellido != null && !segundoApellido.isBlank() ? " " + segundoApellido : "")).trim();

            String uploadedFotoKey = null;
            if (fotoFile != null && !fotoFile.isEmpty()) {
                storageService.validateSize(fotoFile, 10 * 1024 * 1024);
                storageService.validateExtension(fotoFile, "jpg", "jpeg", "png");
                UploadResult result = storageService.upload(fotoFile, StorageFolder.DOCENTES, numDoc);
                uploadedFotoKey = result.getKey();
            }

            if ("Docente".equalsIgnoreCase(cargo)) {
                Docente d = new Docente();
                d.setNombres(nombresFull);
                d.setApellidos(apellidosFull);
                d.setTipoDocumento(tipoDoc);
                d.setNumeroDocumento(numDoc);
                d.setGenero(sexo);
                d.setTelefono(telefono);
                d.setDireccion(direccion);
                d.setTitulo(profesion);
                d.setEspecialidad(especialidad);
                d.setFotoKey(uploadedFotoKey);
                d.setEstado("Activo");
                personalService.guardarDocente(d);
            } else {
                PersonalAdministrativo p = new PersonalAdministrativo();
                p.setNombres(nombresFull);
                p.setApellidos(apellidosFull);
                p.setTipoDocumento(tipoDoc);
                p.setNumeroDocumento(numDoc);
                p.setCargo(cargo);
                p.setArea(area != null ? area : "Administrativa");
                p.setGenero(sexo);
                p.setTelefono(telefono);
                p.setDireccion(direccion);
                p.setFotoKey(uploadedFotoKey);
                p.setEstado("Activo");
                personalService.guardarPersonal(p);
            }

            if (createUser) {
                String pass = (passwordTemp != null && !passwordTemp.isBlank()) ? passwordTemp : "IEACI" + java.time.Year.now().getValue() + "*";
                String targetRole = (role != null && !role.isBlank()) ? role : ("Docente".equalsIgnoreCase(cargo) ? "DOCENTE" : "ADMIN");
                personalService.crearOCambiarCuentaAcceso(numDoc, nombresFull, apellidosFull, userEmail, pass, targetRole);
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
                staff.put("usuarioRol", u.getRol());
                staff.put("usuarioEstado", u.getEstado());
            } else {
                staff.put("tieneCuenta", false);
                staff.put("correo", "Sin cuenta creada");
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
            staff.put("estado", p.getEstado() != null ? p.getEstado() : "Activo");
            staff.put("foto", p.getFotoKey() != null ? "/storage/public/view?key=" + p.getFotoKey() : null);

            Optional<Usuario> usrOpt = personalService.obtenerUsuarioAcceso(p.getNumeroDocumento());
            if (usrOpt.isPresent()) {
                Usuario u = usrOpt.get();
                staff.put("tieneCuenta", true);
                staff.put("correo", u.getEmail());
                staff.put("usuarioRol", u.getRol());
                staff.put("usuarioEstado", u.getEstado());
            } else {
                staff.put("tieneCuenta", false);
                staff.put("correo", "Sin cuenta creada");
                staff.put("emailSugerido", personalService.generarEmailSugerido(p.getNombres(), p.getApellidos()));
            }
        } else {
            // Render full sample staff profile
            if ("2".equals(id)) {
                staff.put("id", "2");
                staff.put("nombreCompleto", "Ana María Sánchez");
                staff.put("primerNombre", "Ana María");
                staff.put("primerApellido", "Sánchez");
                staff.put("cargo", "Coordinador");
                staff.put("area", "Académica");
                staff.put("tipoDoc", "CC");
                staff.put("numDoc", "1.045.321.456");
                staff.put("telefono", "311 456 7890");
                staff.put("direccion", "Calle 45 # 12 - 34");
                staff.put("profesion", "Licenciada en Educación");
                staff.put("especialidad", "Gestión Educativa");
                staff.put("estado", "Activo");
                staff.put("correo", "ana.sanchez@ieaci.edu.co");
                staff.put("tieneCuenta", true);
                staff.put("usuarioRol", "COORDINADOR");
                staff.put("usuarioEstado", "Activo");
                staff.put("foto", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&h=120&q=80");
            } else {
                staff.put("id", id);
                staff.put("nombreCompleto", "Carlos Mendoza");
                staff.put("primerNombre", "Carlos");
                staff.put("primerApellido", "Mendoza");
                staff.put("cargo", "Docente");
                staff.put("area", "Académica");
                staff.put("tipoDoc", "CC");
                staff.put("numDoc", "1.088.123.456");
                staff.put("telefono", "300 123 4567");
                staff.put("direccion", "Manzana A Lote 5, San José");
                staff.put("profesion", "Licenciado en Matemáticas");
                staff.put("especialidad", "Álgebra y Cálculo");
                staff.put("estado", "Activo");
                staff.put("correo", "carlos.mendoza@ieaci.edu.co");
                staff.put("tieneCuenta", true);
                staff.put("usuarioRol", "DOCENTE");
                staff.put("usuarioEstado", "Activo");
                staff.put("foto", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&h=120&q=80");
            }
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

    private Map<String, Object> createSampleStaff(String id, String nombres, String apellidos, String doc, String cargo, String tipo, String correo, String estado, String foto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("nombres", nombres);
        map.put("apellidos", apellidos);
        map.put("nombreCompleto", nombres + " " + apellidos);
        map.put("tipoDocumento", "CC");
        map.put("numeroDocumento", doc);
        map.put("cargo", cargo);
        map.put("tipo", tipo);
        map.put("email", correo);
        map.put("correo", correo);
        map.put("telefono", "3001234567");
        map.put("estado", estado);
        map.put("tieneCuenta", true);
        map.put("foto", foto);
        return map;
    }
}
