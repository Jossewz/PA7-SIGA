package com.siga.siga_iea.clases;

import com.siga.siga_iea.clases.entity.Bloque;
import com.siga.siga_iea.clases.entity.Curso;
import com.siga.siga_iea.clases.entity.Horario;
import com.siga.siga_iea.clases.entity.Salon;
import com.siga.siga_iea.clases.repository.BloqueRepository;
import com.siga.siga_iea.clases.repository.CursoRepository;
import com.siga.siga_iea.clases.repository.HorarioRepository;
import com.siga.siga_iea.clases.repository.SalonRepository;
import com.siga.siga_iea.clases.service.CursoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SalonBloqueTest {

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private BloqueRepository bloqueRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private CursoService cursoService;

    @Test
    @Transactional
    @DisplayName("Debe persistir y listar Salones fisicos y Bloques horarios correctamente")
    void testCreacionYListadoSalonesYBloques() {
        Salon salon = salonRepository.save(new Salon(
                "TEST-LAB-01", "Laboratorio de Fisica", 30, "Edificio Ciencias", "LABORATORIO", "Activo"
        ));
        assertNotNull(salon.getId());
        assertEquals("TEST-LAB-01", salon.getCodigo());

        Bloque bloque = bloqueRepository.save(new Bloque(
                99, "Bloque Especial Tarde", LocalTime.of(14, 0), LocalTime.of(15, 30), "Tarde", "CLASE", "Activo"
        ));
        assertNotNull(bloque.getId());
        assertEquals(99, bloque.getNumero());
        assertEquals("Tarde", bloque.getJornada());

        Optional<Salon> encontrado = salonRepository.findByCodigo("TEST-LAB-01");
        assertTrue(encontrado.isPresent());
        assertEquals("Laboratorio de Fisica", encontrado.get().getNombre());

        Optional<Bloque> bloqueEncontrado = bloqueRepository.findByNumeroAndJornada(99, "Tarde");
        assertTrue(bloqueEncontrado.isPresent());
        assertEquals(LocalTime.of(14, 0), bloqueEncontrado.get().getHoraInicio());
    }

    @Test
    @Transactional
    @DisplayName("CursoService debe vincular Salon y Bloque al guardar un horario")
    void testCursoServiceAsociaSalonYBloque() {
        Salon salon = salonRepository.save(new Salon(
                "AULA-999", "Aula 999", 35, "Bloque Principal", "AULA", "Activo"
        ));
        Bloque bloque = bloqueRepository.save(new Bloque(
                88, "Bloque 88", LocalTime.of(7, 0), LocalTime.of(8, 30), "Mañana", "CLASE", "Activo"
        ));

        Curso curso = cursoService.crearCurso("11°", "05", "Mañana", 35, null, "2026");

        Horario horario = cursoService.guardarHorarioBloque(
                curso.getId(), "Lunes", null, null, "07:00", "08:30", "AULA-999"
        );

        assertNotNull(horario.getId());
        assertEquals("AULA-999", horario.getSalon());
        assertNotNull(horario.getSalonEntidad());
        assertEquals("AULA-999", horario.getSalonEntidad().getCodigo());
        assertNotNull(horario.getBloque());
        assertEquals(88, horario.getBloque().getNumero());
    }
}
