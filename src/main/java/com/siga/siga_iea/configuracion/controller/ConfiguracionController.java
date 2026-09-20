package com.siga.siga_iea.configuracion.controller;

import com.siga.siga_iea.clases.entity.Materia;
import com.siga.siga_iea.clases.repository.MateriaRepository;
import com.siga.siga_iea.configuracion.entity.ConfiguracionInstitucional;
import com.siga.siga_iea.configuracion.entity.EscalaDesempeno;
import com.siga.siga_iea.configuracion.entity.PeriodoAcademico;
import com.siga.siga_iea.configuracion.entity.RolPermiso;
import com.siga.siga_iea.configuracion.service.EscalaDesempenoService;
import com.siga.siga_iea.configuracion.service.PeriodoConfigService;
import com.siga.siga_iea.configuracion.service.RolPermisoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class ConfiguracionController {

    private final PeriodoConfigService periodoConfigService;
    private final EscalaDesempenoService escalaDesempenoService;
    private final RolPermisoService rolPermisoService;
    private final MateriaRepository materiaRepository;

    public ConfiguracionController(PeriodoConfigService periodoConfigService,
                                   EscalaDesempenoService escalaDesempenoService,
                                   RolPermisoService rolPermisoService,
                                   MateriaRepository materiaRepository) {
        this.periodoConfigService = periodoConfigService;
        this.escalaDesempenoService = escalaDesempenoService;
        this.rolPermisoService = rolPermisoService;
        this.materiaRepository = materiaRepository;
    }

    @GetMapping("/configuracion")
    public String index(@RequestParam(value = "rol", defaultValue = "ADMIN") String rolParam,
                        Model model) {
        model.addAttribute("title", "Configuración General – IEACI");
        model.addAttribute("activePage", "configuracion");

        // 1. Datos Institucionales
        ConfiguracionInstitucional cfg = periodoConfigService.obtenerConfiguracionInstitucional();
        model.addAttribute("nit", cfg.getNit());
        model.addAttribute("nombreInst", cfg.getNombreInst());
        model.addAttribute("direccionInst", cfg.getDireccionInst());
        model.addAttribute("telefonoInst", cfg.getTelefonoInst());
        model.addAttribute("correoInst", cfg.getCorreoInst());
        model.addAttribute("anoLectivo", cfg.getAnoLectivo());

        // 2. Parámetros del Sistema (Periodos)
        List<PeriodoAcademico> periodos = periodoConfigService.listarPeriodos();
        model.addAttribute("periodos", periodos);
        model.addAttribute("pesoP1", periodoConfigService.getPesoPeriodo1());
        model.addAttribute("pesoP2", periodoConfigService.getPesoPeriodo2());
        model.addAttribute("pesoP3", periodoConfigService.getPesoPeriodo3());
        model.addAttribute("fIniP1", periodoConfigService.getFechaInicioP1());
        model.addAttribute("fFinP1", periodoConfigService.getFechaFinP1());
        model.addAttribute("fIniP2", periodoConfigService.getFechaInicioP2());
        model.addAttribute("fFinP2", periodoConfigService.getFechaFinP2());
        model.addAttribute("fIniP3", periodoConfigService.getFechaInicioP3());
        model.addAttribute("fFinP3", periodoConfigService.getFechaFinP3());

        // 3. Escala de Desempeño
        List<EscalaDesempeno> escalas = escalaDesempenoService.listarEscalaCompleta();
        model.addAttribute("escalas", escalas);

        // 4. Asignaturas del Colegio
        List<Materia> materias = materiaRepository.findAllByOrderByNombreAsc();
        model.addAttribute("materias", materias);

        // 5. Roles y Permisos
        List<String> rolesDisponibles = List.of("ADMIN", "PERSONAL_ADMINISTRATIVO", "DOCENTE", "ESTUDIANTE");
        String rolSeleccionado = rolesDisponibles.contains(rolParam) ? rolParam : "ADMIN";
        List<RolPermiso> permisosRol = rolPermisoService.listarPermisosPorRol(rolSeleccionado);
        model.addAttribute("rolesDisponibles", rolesDisponibles);
        model.addAttribute("rolSeleccionado", rolSeleccionado);
        model.addAttribute("permisosRol", permisosRol);

        return "configuracion/index";
    }

    @PostMapping("/configuracion/institucion")
    public String guardarInstitucion(
            @RequestParam("nit") String nit,
            @RequestParam("nombreInst") String nombreInst,
            @RequestParam("direccionInst") String direccionInst,
            @RequestParam("telefonoInst") String telefonoInst,
            @RequestParam("correoInst") String correoInst,
            @RequestParam("anoLectivo") String anoLectivo,
            RedirectAttributes redirectAttributes) {

        periodoConfigService.guardarConfiguracionInstitucional(nit, nombreInst, direccionInst, telefonoInst, correoInst, anoLectivo);
        redirectAttributes.addFlashAttribute("mensajeExito", "Información institucional actualizada exitosamente.");
        redirectAttributes.addFlashAttribute("activeTab", "cfg-institucion");
        return "redirect:/configuracion";
    }

    @PostMapping("/configuracion/periodos")
    public String guardarPeriodos(
            @RequestParam("pesoP1") BigDecimal pesoP1,
            @RequestParam("pesoP2") BigDecimal pesoP2,
            @RequestParam("pesoP3") BigDecimal pesoP3,
            @RequestParam("fIniP1") String fIniP1,
            @RequestParam("fFinP1") String fFinP1,
            @RequestParam("fIniP2") String fIniP2,
            @RequestParam("fFinP2") String fFinP2,
            @RequestParam("fIniP3") String fIniP3,
            @RequestParam("fFinP3") String fFinP3,
            RedirectAttributes redirectAttributes) {

        BigDecimal suma = pesoP1.add(pesoP2).add(pesoP3);
        if (suma.compareTo(new BigDecimal("100")) != 0) {
            redirectAttributes.addFlashAttribute("mensajeError", "La suma de las ponderaciones de los períodos debe ser exactamente 100% (Suma actual: " + suma + "%).");
            redirectAttributes.addFlashAttribute("activeTab", "cfg-parametros");
            return "redirect:/configuracion";
        }

        periodoConfigService.actualizarPonderaciones(pesoP1, pesoP2, pesoP3, fIniP1, fFinP1, fIniP2, fFinP2, fIniP3, fFinP3);
        redirectAttributes.addFlashAttribute("mensajeExito", "Ponderaciones y fechas de los 3 períodos académicos guardadas exitosamente.");
        redirectAttributes.addFlashAttribute("activeTab", "cfg-parametros");
        return "redirect:/configuracion";
    }

    @PostMapping("/configuracion/escala-desempeno")
    public String guardarEscalaDesempeno(
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {

        try {
            List<EscalaDesempeno> escalas = escalaDesempenoService.listarEscalaCompleta();
            for (EscalaDesempeno e : escalas) {
                String cod = e.getCodigo();
                String minStr = allParams.get("min_" + cod);
                String maxStr = allParams.get("max_" + cod);
                String nomStr = allParams.get("nom_" + cod);

                if (minStr != null && maxStr != null) {
                    BigDecimal min = new BigDecimal(minStr.trim());
                    BigDecimal max = new BigDecimal(maxStr.trim());
                    if (min.compareTo(max) > 0) {
                        redirectAttributes.addFlashAttribute("mensajeError", "El valor mínimo no puede ser mayor al valor máximo en " + e.getNombre());
                        redirectAttributes.addFlashAttribute("activeTab", "cfg-parametros");
                        return "redirect:/configuracion";
                    }
                    escalaDesempenoService.actualizarRangoEscala(cod, min, max, nomStr);
                }
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Escala de desempeño y rangos de calificaciones guardados exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar escala: " + ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("activeTab", "cfg-parametros");
        return "redirect:/configuracion";
    }

    @PostMapping("/configuracion/materias/guardar")
    public String guardarMateria(
            @RequestParam(value = "id", required = false) UUID id,
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "area", required = false) String area,
            @RequestParam(value = "intensidadHoraria", defaultValue = "4") Integer intensidadHoraria,
            @RequestParam(value = "estado", defaultValue = "Activo") String estado,
            RedirectAttributes redirectAttributes) {

        try {
            Materia materia;
            if (id != null) {
                materia = materiaRepository.findById(id).orElse(new Materia());
            } else {
                materia = new Materia();
            }
            materia.setNombre(nombre.trim());
            materia.setArea(area != null ? area.trim() : "General");
            materia.setIntensidadHoraria(intensidadHoraria != null ? intensidadHoraria : 4);
            materia.setEstado(estado);
            materiaRepository.save(materia);

            redirectAttributes.addFlashAttribute("mensajeExito", "Asignatura '" + materia.getNombre() + "' guardada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo guardar la asignatura: " + ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("activeTab", "cfg-asignaturas");
        return "redirect:/configuracion";
    }

    @PostMapping("/configuracion/materias/toggle-estado")
    public String toggleEstadoMateria(
            @RequestParam("id") UUID id,
            RedirectAttributes redirectAttributes) {

        materiaRepository.findById(id).ifPresent(m -> {
            String nuevoEstado = "Activo".equalsIgnoreCase(m.getEstado()) ? "Inactivo" : "Activo";
            m.setEstado(nuevoEstado);
            materiaRepository.save(m);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado de '" + m.getNombre() + "' cambiado a " + nuevoEstado + ".");
        });

        redirectAttributes.addFlashAttribute("activeTab", "cfg-asignaturas");
        return "redirect:/configuracion";
    }

    @PostMapping("/configuracion/roles/permisos")
    public String guardarPermisosRol(
            @RequestParam("rol") String rol,
            @RequestParam(value = "modulosAcceso", required = false) List<String> modulosAcceso,
            @RequestParam(value = "modulosEdicion", required = false) List<String> modulosEdicion,
            @RequestParam(value = "modulosEliminacion", required = false) List<String> modulosEliminacion,
            RedirectAttributes redirectAttributes) {

        Set<String> acceso = modulosAcceso != null ? new HashSet<>(modulosAcceso) : Collections.emptySet();
        Set<String> edicion = modulosEdicion != null ? new HashSet<>(modulosEdicion) : Collections.emptySet();
        Set<String> eliminacion = modulosEliminacion != null ? new HashSet<>(modulosEliminacion) : Collections.emptySet();

        rolPermisoService.actualizarPermisosRol(rol, acceso, edicion, eliminacion);
        redirectAttributes.addFlashAttribute("mensajeExito", "Permisos actualizados correctamente para el rol: " + rol);
        redirectAttributes.addFlashAttribute("activeTab", "cfg-roles");
        redirectAttributes.addAttribute("rol", rol);
        redirectAttributes.addAttribute("tab", "cfg-roles");
        return "redirect:/configuracion";
    }

    @GetMapping("/configuracion/roles/permisos-fragment")
    public String obtenerFragmentoPermisos(@RequestParam("rol") String rol, Model model) {
        List<RolPermiso> permisos = rolPermisoService.listarPermisosPorRol(rol);
        model.addAttribute("permisosRol", permisos);
        model.addAttribute("rolSeleccionado", rol);
        return "configuracion/fragments/panel-permisos :: panelPermisos";
    }
}
