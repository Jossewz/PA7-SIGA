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
                new EscalaDesempeno("ALTO", "Desempeño Alto", new BigDecimal("4.00"), new BigDecimal("4.59"), "bg-emerald-600 text-white", "#059669", 2),
                new EscalaDesempeno("BASICO", "Desempeño Básico", new BigDecimal("3.00"), new BigDecimal("3.99"), "bg-amber-500 text-white", "#f59e0b", 3),
                new EscalaDesempeno("BAJO", "Desempeño Bajo", new BigDecimal("2.00"), new BigDecimal("2.99"), "bg-orange-500 text-white", "#f97316", 4),
                new EscalaDesempeno("MUY_BAJO", "Desempeño Muy Bajo", new BigDecimal("1.00"), new BigDecimal("1.99"), "bg-rose-500 text-white", "#f43f5e", 5),
                new EscalaDesempeno("CRITICO", "Desempeño Crítico", new BigDecimal("0.00"), new BigDecimal("0.99"), "bg-red-700 text-white", "#b91c1c", 6)
        );
        return escalaDesempenoRepository.saveAll(defaultList);
    }

    public EscalaDesempeno obtenerEscalaPorNota(BigDecimal nota) {
        if (nota == null) {
            nota = BigDecimal.ZERO;
        }
        BigDecimal notaRedondeada = nota.setScale(2, RoundingMode.HALF_UP);
        List<EscalaDesempeno> escalas = listarEscalaCompleta();

        for (EscalaDesempeno e : escalas) {
            if (notaRedondeada.compareTo(e.getNotaMinima()) >= 0 && notaRedondeada.compareTo(e.getNotaMaxima()) <= 0) {
                return e;
            }
        }

        // Si excede 5.0 o hay un borde mínimo, aproximar al más cercano
        if (notaRedondeada.compareTo(new BigDecimal("5.00")) >= 0 && !escalas.isEmpty()) {
            return escalas.get(0);
        }
        if (!escalas.isEmpty()) {
            return escalas.get(escalas.size() - 1);
        }

        return new EscalaDesempeno("CRITICO", "Desempeño Crítico", BigDecimal.ZERO, new BigDecimal("0.99"), "bg-red-700 text-white", "#b91c1c", 6);
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
