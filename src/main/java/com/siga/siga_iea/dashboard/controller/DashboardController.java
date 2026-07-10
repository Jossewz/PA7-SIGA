package com.siga.siga_iea.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String dashboard(Model model){

        model.addAttribute(
                "title",
                "Dashboard"
        );

        return "dashboard/index";
    }

}
