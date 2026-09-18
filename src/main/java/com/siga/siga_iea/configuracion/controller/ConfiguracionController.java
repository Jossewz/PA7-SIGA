package com.siga.siga_iea.configuracion.controller;

import com.siga.siga_iea.configuracion.service.PeriodoConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class ConfiguracionController {

    private final PeriodoConfigService periodoConfigService;

    public ConfiguracionController(PeriodoConfigService periodoConfigService) {
        this.periodoConfigService = periodoConfigService;
    }

    @GetMapping("/configuracion")
    public String index(Model model) {
        model.addAttribute("title", "Configuración General – IEACI");
        model.addAttribute("activePage", "configuracion");
        
        // Institutional data
        model.addAttribute("nit", "800.124.567-2");
        model.addAttribute("nombreInst", "Institución Educativa Ateneo de la Ciencia e Innovación (IEACI)");
        model.addAttribute("direccionInst", "San José de los Campanos, Mz 32 Lote 9-11");
        model.addAttribute("telefonoInst", "300 987 6543");
        model.addAttribute("correoInst", "contacto@ieaci.edu.co");
        model.addAttribute("añoLectivo", "2026");

        // Period configurations
        model.addAttribute("pesoP1", periodoConfigService.getPesoPeriodo1());
        model.addAttribute("pesoP2", periodoConfigService.getPesoPeriodo2());
        model.addAttribute("pesoP3", periodoConfigService.getPesoPeriodo3());
        model.addAttribute("fIniP1", periodoConfigService.getFechaInicioP1());
        model.addAttribute("fFinP1", periodoConfigService.getFechaFinP1());
        model.addAttribute("fIniP2", periodoConfigService.getFechaInicioP2());
        model.addAttribute("fFinP2", periodoConfigService.getFechaFinP2());
        model.addAttribute("fIniP3", periodoConfigService.getFechaInicioP3());
        model.addAttribute("fFinP3", periodoConfigService.getFechaFinP3());

        return "configuracion/index";
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
            redirectAttributes.addFlashAttribute("activeTab", "cfg-calendario");
            return "redirect:/configuracion";
        }

        periodoConfigService.actualizarPonderaciones(pesoP1, pesoP2, pesoP3, fIniP1, fFinP1, fIniP2, fFinP2, fIniP3, fFinP3);
        redirectAttributes.addFlashAttribute("mensajeExito", "Ponderaciones y fechas de los 3 períodos académicos guardadas exitosamente.");
        redirectAttributes.addFlashAttribute("activeTab", "cfg-calendario");
        return "redirect:/configuracion";
    }
}
