package com.siga.siga_iea.configuracion.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PeriodoConfigService {

    private BigDecimal pesoPeriodo1 = new BigDecimal("30.00");
    private BigDecimal pesoPeriodo2 = new BigDecimal("35.00");
    private BigDecimal pesoPeriodo3 = new BigDecimal("35.00");

    private String fechaInicioP1 = "2026-02-01";
    private String fechaFinP1 = "2026-06-15";
    private String fechaInicioP2 = "2026-07-15";
    private String fechaFinP2 = "2026-09-15";
    private String fechaInicioP3 = "2026-09-16";
    private String fechaFinP3 = "2026-11-30";

    public Map<Integer, BigDecimal> getPonderaciones() {
        Map<Integer, BigDecimal> map = new HashMap<>();
        map.put(1, pesoPeriodo1);
        map.put(2, pesoPeriodo2);
        map.put(3, pesoPeriodo3);
        return map;
    }

    public synchronized void actualizarPonderaciones(BigDecimal p1, BigDecimal p2, BigDecimal p3,
                                                     String fIni1, String fFin1,
                                                     String fIni2, String fFin2,
                                                     String fIni3, String fFin3) {
        if (p1 != null) this.pesoPeriodo1 = p1;
        if (p2 != null) this.pesoPeriodo2 = p2;
        if (p3 != null) this.pesoPeriodo3 = p3;
        if (fIni1 != null) this.fechaInicioP1 = fIni1;
        if (fFin1 != null) this.fechaFinP1 = fFin1;
        if (fIni2 != null) this.fechaInicioP2 = fIni2;
        if (fFin2 != null) this.fechaFinP2 = fFin2;
        if (fIni3 != null) this.fechaInicioP3 = fIni3;
        if (fFin3 != null) this.fechaFinP3 = fFin3;
    }

    public BigDecimal getPesoPeriodo1() { return pesoPeriodo1; }
    public BigDecimal getPesoPeriodo2() { return pesoPeriodo2; }
    public BigDecimal getPesoPeriodo3() { return pesoPeriodo3; }

    public String getFechaInicioP1() { return fechaInicioP1; }
    public String getFechaFinP1() { return fechaFinP1; }
    public String getFechaInicioP2() { return fechaInicioP2; }
    public String getFechaFinP2() { return fechaFinP2; }
    public String getFechaInicioP3() { return fechaInicioP3; }
    public String getFechaFinP3() { return fechaFinP3; }
}
