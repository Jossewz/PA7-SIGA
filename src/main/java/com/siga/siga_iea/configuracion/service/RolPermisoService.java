package com.siga.siga_iea.configuracion.service;

import com.siga.siga_iea.auth.security.CustomUserDetailsService;
import com.siga.siga_iea.configuracion.entity.RolPermiso;
import com.siga.siga_iea.configuracion.repository.RolPermisoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolPermisoService {

    private final RolPermisoRepository rolPermisoRepository;

    private static final Map<String, String[]> MODULOS_INFO = Map.of(
            "MATRICULAS", new String[]{"Matrículas y Estudiantes", "Gestión y consulta de matrículas y expedientes académicos"},
            "PERSONAL", new String[]{"Directorio de Personal", "Directorio institucional de docentes y personal administrativo"},
            "CURSOS_HORARIOS", new String[]{"Cursos y Horarios", "Gestión y consulta de cursos, asignaturas y horarios"},
            "CALIFICACIONES", new String[]{"Calificaciones y Evaluaciones", "Consulta y registro de calificaciones, evaluaciones y boletines"},
            "ASISTENCIAS", new String[]{"Control de Asistencias", "Control y registro de asistencia a clases"},
            "CERTIFICADOS", new String[]{"Certificados y Constancias", "Solicitud y expedición de certificados de estudio y constancias"},
            "REPORTES", new String[]{"Reportes y Estadísticas", "Generación de reportes institucionales y analítica académica"},
            "CONFIGURACION", new String[]{"Configuración del Sistema", "Parámetros institucionales, periodos, escala de notas y permisos"},
            "MODULO_AMBIENTAL", new String[]{"Módulo Ambiental PRAE", "Proyectos ecológicos y evidencias ambientales PRAE"}
    );

    public RolPermisoService(RolPermisoRepository rolPermisoRepository) {
        this.rolPermisoRepository = rolPermisoRepository;
    }

    public List<RolPermiso> listarPermisosPorRol(String rawRol) {
        String rol = CustomUserDetailsService.normalizeRole(rawRol);
        List<RolPermiso> permisos = rolPermisoRepository.findByRolOrderByModuloAsc(rol);

        // Si no existen permisos para el rol, o falta algún módulo, autocompletar
        if (permisos.size() < MODULOS_INFO.size()) {
            permisos = asegurarModulosCompletosParaRol(rol, permisos);
        }

        // Limpiar cualquier descripción obsoleta que diga "No autorizado"
        for (RolPermiso p : permisos) {
            if (p.getDescripcion() == null || "No autorizado".equalsIgnoreCase(p.getDescripcion().trim())) {
                String[] info = MODULOS_INFO.get(p.getModulo());
                if (info != null) {
                    p.setDescripcion(info[1]);
                    p.setNombreModulo(info[0]);
                }
            }
        }
        return permisos;
    }

    @Transactional
    public List<RolPermiso> asegurarModulosCompletosParaRol(String rol, List<RolPermiso> existentes) {
        Set<String> modulosExistentes = existentes.stream().map(RolPermiso::getModulo).collect(Collectors.toSet());
        List<RolPermiso> aGuardar = new ArrayList<>(existentes);

        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);
        for (Map.Entry<String, String[]> entry : MODULOS_INFO.entrySet()) {
            String mod = entry.getKey();
            if (!modulosExistentes.contains(mod)) {
                String[] info = entry.getValue();
                boolean defaultAcceso = defaultAccesoParaRol(rol, mod);
                boolean defaultEdicion = defaultEdicionParaRol(rol, mod);
                boolean defaultEliminacion = esAdmin;
                RolPermiso nuevo = new RolPermiso(
                        rol,
                        mod,
                        info[0],
                        info[1],
                        defaultAcceso,
                        defaultEdicion,
                        defaultEliminacion
                );
                aGuardar.add(rolPermisoRepository.save(nuevo));
            }
        }
        return aGuardar;
    }

    private boolean defaultAccesoParaRol(String rol, String mod) {
        if ("ADMIN".equalsIgnoreCase(rol) || "PERSONAL_ADMINISTRATIVO".equalsIgnoreCase(rol)) return true;
        if ("DOCENTE".equalsIgnoreCase(rol)) {
            return switch (mod) {
                case "MATRICULAS", "CURSOS_HORARIOS", "CALIFICACIONES", "ASISTENCIAS", "REPORTES", "MODULO_AMBIENTAL" -> true;
                default -> false;
            };
        }
        if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
            return switch (mod) {
                case "MATRICULAS", "CURSOS_HORARIOS", "CALIFICACIONES", "ASISTENCIAS", "CERTIFICADOS", "MODULO_AMBIENTAL" -> true;
                default -> false;
            };
        }
        return false;
    }

    private boolean defaultEdicionParaRol(String rol, String mod) {
        if ("ADMIN".equalsIgnoreCase(rol)) return true;
        if ("PERSONAL_ADMINISTRATIVO".equalsIgnoreCase(rol)) {
            return !"CALIFICACIONES".equals(mod) && !"ASISTENCIAS".equals(mod) && !"CONFIGURACION".equals(mod);
        }
        if ("DOCENTE".equalsIgnoreCase(rol)) {
            return "CALIFICACIONES".equals(mod) || "ASISTENCIAS".equals(mod) || "MODULO_AMBIENTAL".equals(mod);
        }
        return false;
    }

    @Transactional
    public void actualizarPermisosRol(String rawRol,
                                      Set<String> modulosAcceder,
                                      Set<String> modulosEditar,
                                      Set<String> modulosEliminar) {
        String rol = CustomUserDetailsService.normalizeRole(rawRol);
        List<RolPermiso> permisos = listarPermisosPorRol(rol);

        for (RolPermiso p : permisos) {
            boolean acceso = modulosAcceder != null && modulosAcceder.contains(p.getModulo());
            boolean edicion = modulosEditar != null && modulosEditar.contains(p.getModulo());
            boolean eliminacion = modulosEliminar != null && modulosEliminar.contains(p.getModulo());

            p.setPuedeAcceder(acceso);
            p.setPuedeEditar(edicion);
            p.setPuedeEliminar(eliminacion);

            // Asegurar descripción amigable
            String[] info = MODULOS_INFO.get(p.getModulo());
            if (info != null) {
                p.setNombreModulo(info[0]);
                p.setDescripcion(info[1]);
            }
        }
        rolPermisoRepository.saveAll(permisos);
    }

    public boolean tieneAcceso(String rawRol, String modulo) {
        if (rawRol == null || modulo == null) return false;
        String rol = CustomUserDetailsService.normalizeRole(rawRol);
        if ("ADMIN".equalsIgnoreCase(rol)) return true;

        Optional<RolPermiso> opt = rolPermisoRepository.findByRolAndModulo(rol, modulo);
        if (opt.isEmpty()) {
            asegurarModulosCompletosParaRol(rol, rolPermisoRepository.findByRolOrderByModuloAsc(rol));
            opt = rolPermisoRepository.findByRolAndModulo(rol, modulo);
        }

        return opt.map(RolPermiso::getPuedeAcceder).orElse(false);
    }

    public Set<String> obtenerModulosPermitidos(String rawRol) {
        if (rawRol == null) return Collections.emptySet();
        String rol = CustomUserDetailsService.normalizeRole(rawRol);

        if ("ADMIN".equalsIgnoreCase(rol)) {
            return new HashSet<>(MODULOS_INFO.keySet());
        }

        List<RolPermiso> lista = rolPermisoRepository.findByRolOrderByModuloAsc(rol);
        if (lista.isEmpty()) {
            lista = asegurarModulosCompletosParaRol(rol, lista);
        }

        return lista.stream()
                .filter(RolPermiso::getPuedeAcceder)
                .map(RolPermiso::getModulo)
                .collect(Collectors.toSet());
    }
}
