package com.plataforma.colegio.dashboard.service;

import com.plataforma.colegio.dashboard.dto.DashboardStatsDTO;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    public DashboardStatsDTO getStats() {
        return new DashboardStatsDTO(0, 0, 0);
    }
}
