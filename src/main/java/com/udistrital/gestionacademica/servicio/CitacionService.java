package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Acudiente;
import com.udistrital.gestionacademica.modelo.Citacion;
import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.repositorio.AcudienteRepository;
import com.udistrital.gestionacademica.repositorio.CitacionRepository;
import com.udistrital.gestionacademica.repositorio.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CitacionService {

    private final CitacionRepository citacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final AcudienteRepository acudienteRepository;
    private final emailService emailService;

    /**
     * Crear una citación individual y enviar correo
     */
    public Citacion crearCitacion(Long codigoEstudiante, LocalDateTime fechaCitacion) {
        log.info("Creando citación para estudiante: {}", codigoEstudiante);

        Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        if (estudiante.getAcudiente() == null) {
            throw new RuntimeException("El estudiante no tiene acudiente asignado");
        }

        Citacion citacion = new Citacion();
        citacion.setEstudiante(estudiante);
        citacion.setAcudiente(estudiante.getAcudiente());
        citacion.setFechaCitacion(fechaCitacion);

        Citacion citacionGuardada = citacionRepository.save(citacion);
        log.info("Citación creada exitosamente con ID: {}", citacionGuardada.getIdCitacion());

        // Enviar correo electrónico de forma asíncrona
        try {
            emailService.enviarCorreoCitacion(citacionGuardada);
            log.info("Correo de citación programado para envío al acudiente: {}",
                    estudiante.getAcudiente().getCorreoElectronico());
        } catch (Exception e) {
            log.error("Error al programar envío de correo para citación {}: {}",
                    citacionGuardada.getIdCitacion(), e.getMessage());
            // No lanzamos excepción para no interrumpir el proceso de creación
        }

        return citacionGuardada;
    }

    /**
     * Crear múltiples citaciones para varios estudiantes y enviar correos
     */
    public List<Citacion> crearCitacionesMultiples(List<Long> codigosEstudiantes, LocalDateTime fechaCitacion) {
        log.info("Creando {} citaciones masivas", codigosEstudiantes.size());

        List<Citacion> citacionesCreadas = new ArrayList<>();
        int correosEnviados = 0;
        int correosFallidos = 0;

        for (Long codigoEstudiante : codigosEstudiantes) {
            try {
                Citacion citacion = crearCitacion(codigoEstudiante, fechaCitacion);
                citacionesCreadas.add(citacion);
                correosEnviados++;
            } catch (RuntimeException e) {
                log.warn("Error al crear citación para estudiante {}: {}", codigoEstudiante, e.getMessage());
                correosFallidos++;
            }
        }

        log.info("Proceso completado: {} citaciones creadas, {} correos programados, {} fallos",
                citacionesCreadas.size(), correosEnviados, correosFallidos);

        return citacionesCreadas;
    }

    /**
     * Obtener citaciones por acudiente
     */
    @Transactional(readOnly = true)
    public List<Citacion> obtenerCitacionesPorAcudiente(Long idAcudiente) {
        log.info("Obteniendo citaciones del acudiente: {}", idAcudiente);
        return citacionRepository.findByAcudienteId(idAcudiente);
    }

    /**
     * Obtener citaciones por estudiante
     */
    @Transactional(readOnly = true)
    public List<Citacion> obtenerCitacionesPorEstudiante(Long codigoEstudiante) {
        log.info("Obteniendo citaciones del estudiante: {}", codigoEstudiante);
        return citacionRepository.findByEstudianteId(codigoEstudiante);
    }

    /**
     * Obtener citaciones por grupo
     */
    @Transactional(readOnly = true)
    public List<Citacion> obtenerCitacionesPorGrupo(Long idGrupo) {
        log.info("Obteniendo citaciones del grupo: {}", idGrupo);
        return citacionRepository.findByGrupoId(idGrupo);
    }

    /**
     * Obtener todas las citaciones
     */
    @Transactional(readOnly = true)
    public List<Citacion> obtenerTodasLasCitaciones() {
        log.info("Obteniendo todas las citaciones");
        return citacionRepository.findAll();
    }

    /**
     * Eliminar una citación
     */
    public void eliminarCitacion(Long idCitacion) {
        log.info("Eliminando citación: {}", idCitacion);

        Citacion citacion = citacionRepository.findById(idCitacion)
                .orElseThrow(() -> new RuntimeException("Citación no encontrada"));

        citacionRepository.delete(citacion);
        log.info("Citación eliminada exitosamente");
    }

    /**
     * Reenviar correo de citación
     */
    public void reenviarCorreoCitacion(Long idCitacion) {
        log.info("Reenviando correo de citación: {}", idCitacion);

        Citacion citacion = citacionRepository.findById(idCitacion)
                .orElseThrow(() -> new RuntimeException("Citación no encontrada"));

        try {
            emailService.enviarCorreoCitacion(citacion);
            log.info("Correo de citación reenviado exitosamente");
        } catch (Exception e) {
            log.error("Error al reenviar correo de citación: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage());
        }
    }
}
