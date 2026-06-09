package com.siga.siga_iea.dashboard.controller;

import com.siga.siga_iea.dashboard.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public String index(Model model) {
        model.addAttribute("stats", service.getStats());
        return "dashboard/index";
    }
}

