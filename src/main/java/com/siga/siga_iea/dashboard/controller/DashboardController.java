package com.siga.siga_iea.dashboard.controller;

import com.siga.siga_iea.dashboard.dto.DashboardStatsDTO;
import com.siga.siga_iea.dashboard.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        DashboardStatsDTO stats = dashboardService.obtenerEstadisticas();

        model.addAttribute("title", "Dashboard – IEACI");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("stats", stats);

        return "dashboard/index";
    }
}
