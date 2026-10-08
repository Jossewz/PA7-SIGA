package com.siga.siga_iea.configuracion.service;

import com.siga.siga_iea.configuracion.entity.EscalaDesempeno;
import com.siga.siga_iea.configuracion.repository.EscalaDesempenoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Service
public class EscalaDesempenoService {

    private final EscalaDesempenoRepository escalaDesempenoRepository;

    public EscalaDesempenoService(EscalaDesempenoRepository escalaDesempenoRepository) {
        this.escalaDesempenoRepository = escalaDesempenoRepository;
    }

    public List<EscalaDesempeno> listarEscalaCompleta() {
        List<EscalaDesempeno> list = escalaDesempenoRepository.findAllByOrderByOrdenAsc();
        if (list.isEmpty()) {
            list = inicializarEscalaPorDefecto();
        }
        return list;
    }

    @Transactional
    public List<EscalaDesempeno> inicializarEscalaPorDefecto() {
        List<EscalaDesempeno> defaultList = Arrays.asList(
                new EscalaDesempeno("SUPERIOR", "Desempeño Superior", new BigDecimal("4.60"), new BigDecimal("5.00"), "bg-sidebar text-white", "#1b5e20", 1),
                new EscalaDesempeno("ALTO", "Desempeño Alto", new BigDecimal("4.00"), new BigDecimal("4.50"), "bg-emerald-600 text-white", "#059669", 2),
                new EscalaDesempeno("BASICO", "Desempeño Básico", new BigDecimal("3.00"), new BigDecimal("3.90"), "bg-amber-500 text-white", "#f59e0b", 3),
                new EscalaDesempeno("BAJO", "Desempeño Bajo", new BigDecimal("1.00"), new BigDecimal("2.90"), "bg-rose-500 text-white", "#f43f5e", 4)
        );
        return escalaDesempenoRepository.saveAll(defaultList);
    }

    public EscalaDesempeno obtenerEscalaPorNota(BigDecimal nota) {
        if (nota == null) {
            nota = BigDecimal.ONE;
        }
        // Redondeo institucional a 1 cifra decimal conforme a la escala SIEACI (Ítem 5.4.11 / Decreto 1290)
        // Ejemplo: 4.55 se redondea a 4.6 (Superior), 3.95 se redondea a 4.0 (Alto), 2.95 se redondea a 3.0 (Básico)
        BigDecimal notaRedondeada = nota.setScale(1, RoundingMode.HALF_UP);
        List<EscalaDesempeno> escalas = listarEscalaCompleta();

        for (EscalaDesempeno e : escalas) {
            BigDecimal min = e.getNotaMinima().setScale(1, RoundingMode.HALF_UP);
            BigDecimal max = e.getNotaMaxima().setScale(1, RoundingMode.HALF_UP);
            if (notaRedondeada.compareTo(min) >= 0 && notaRedondeada.compareTo(max) <= 0) {
                return e;
            }
        }

        // Si excede 5.0, clasificar en Superior (primer elemento orden 1)
        if (notaRedondeada.compareTo(new BigDecimal("5.0")) >= 0 && !escalas.isEmpty()) {
            return escalas.get(0);
        }
        // Si la nota es inferior a 1.0 (o borde inferior), clasificar en Bajo (último elemento orden 4)
        if (!escalas.isEmpty()) {
            return escalas.get(escalas.size() - 1);
        }

        return new EscalaDesempeno("BAJO", "Desempeño Bajo", new BigDecimal("1.00"), new BigDecimal("2.90"), "bg-rose-500 text-white", "#f43f5e", 4);
    }

    public String obtenerBadgeClassPorNota(BigDecimal nota) {
        return obtenerEscalaPorNota(nota).getColorBadge();
    }

    public String obtenerNombreDesempenoPorNota(BigDecimal nota) {
        return obtenerEscalaPorNota(nota).getNombre();
    }

    @Transactional
    public void actualizarRangoEscala(String codigo, BigDecimal min, BigDecimal max, String nombre) {
        escalaDesempenoRepository.findByCodigo(codigo).ifPresent(e -> {
            if (min != null) e.setNotaMinima(min);
            if (max != null) e.setNotaMaxima(max);
            if (nombre != null && !nombre.isBlank()) e.setNombre(nombre.trim());
            escalaDesempenoRepository.save(e);
        });
    }
}
