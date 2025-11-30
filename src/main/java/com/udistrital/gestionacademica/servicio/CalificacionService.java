package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Calificacion;
import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.modelo.Logro;
import com.udistrital.gestionacademica.repositorio.CalificacionRepository;
import com.udistrital.gestionacademica.repositorio.EstudianteRepository;
import com.udistrital.gestionacademica.repositorio.LogroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final LogroRepository logroRepository;

    /**
     * Obtener todas las calificaciones de un estudiante
     */
    @Transactional(readOnly = true)
    public List<Calificacion> obtenerCalificacionesPorEstudiante(Long codigoEstudiante) {
        log.info("Obteniendo calificaciones del estudiante: {}", codigoEstudiante);
        
        // Verificar que el estudiante existe
        Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        return calificacionRepository.findByEstudianteCodigoEstudiante(codigoEstudiante);
    }

    /**
     * Asignar un logro a un estudiante
     */
    public Calificacion asignarLogro(Long codigoEstudiante, Long idLogro, String observaciones) {
        log.info("Asignando logro {} al estudiante {}", idLogro, codigoEstudiante);
        
        // Verificar que el estudiante existe
        Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        // Verificar que el logro existe
        Logro logro = logroRepository.findById(idLogro)
                .orElseThrow(() -> new RuntimeException("Logro no encontrado"));
        
        // Verificar que no se haya asignado ya este logro al estudiante
        Optional<Calificacion> calificacionExistente = 
            calificacionRepository.findByEstudianteAndLogro(codigoEstudiante, idLogro);
        
        if (calificacionExistente.isPresent()) {
            throw new RuntimeException("Este logro ya ha sido asignado al estudiante");
        }
        
        // Crear la nueva calificación
        Calificacion calificacion = new Calificacion();
        calificacion.setEstudiante(estudiante);
        calificacion.setLogro(logro);
        calificacion.setObservaciones(observaciones);
        
        Calificacion nuevaCalificacion = calificacionRepository.save(calificacion);
        log.info("Logro asignado exitosamente con ID: {}", nuevaCalificacion.getIdCalificacion());
        
        return nuevaCalificacion;
    }

    /**
     * Modificar observaciones de una calificación
     */
    public Calificacion modificarCalificacion(Long idCalificacion, String observaciones) {
        log.info("Modificando calificación: {}", idCalificacion);
        
        Calificacion calificacion = calificacionRepository.findById(idCalificacion)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada"));
        
        calificacion.setObservaciones(observaciones);
        
        Calificacion calificacionActualizada = calificacionRepository.save(calificacion);
        log.info("Calificación modificada exitosamente");
        
        return calificacionActualizada;
    }

    /**
     * Eliminar una calificación (desasignar logro)
     */
    public void eliminarCalificacion(Long idCalificacion) {
        log.info("Eliminando calificación: {}", idCalificacion);
        
        Calificacion calificacion = calificacionRepository.findById(idCalificacion)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada"));
        
        calificacionRepository.delete(calificacion);
        log.info("Calificación eliminada exitosamente");
    }

    /**
     * Obtener el historial completo de un estudiante con estadísticas
     */
    @Transactional(readOnly = true)
    public HistorialEstudiante obtenerHistorial(Long codigoEstudiante) {
        log.info("Obteniendo historial del estudiante: {}", codigoEstudiante);
        
        Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        List<Calificacion> calificaciones = 
            calificacionRepository.findByEstudianteCodigoEstudiante(codigoEstudiante);
        
        long totalLogros = calificacionRepository.countByEstudianteCodigoEstudiante(codigoEstudiante);
        
        return new HistorialEstudiante(estudiante, calificaciones, totalLogros);
    }

    /**
     * Clase interna para encapsular el historial del estudiante
     */
    public static class HistorialEstudiante {
        private final Estudiante estudiante;
        private final List<Calificacion> calificaciones;
        private final long totalLogros;

        public HistorialEstudiante(Estudiante estudiante, List<Calificacion> calificaciones, long totalLogros) {
            this.estudiante = estudiante;
            this.calificaciones = calificaciones;
            this.totalLogros = totalLogros;
        }

        public Estudiante getEstudiante() {
            return estudiante;
        }

        public List<Calificacion> getCalificaciones() {
            return calificaciones;
        }

        public long getTotalLogros() {
            return totalLogros;
        }
    }
}