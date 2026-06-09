package com.siga.siga_iea.dashboard.service;

import com.siga.siga_iea.dashboard.dto.DashboardStatsDTO;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    public DashboardStatsDTO getStats() {
        return new DashboardStatsDTO(0, 0, 0);
    }
}

