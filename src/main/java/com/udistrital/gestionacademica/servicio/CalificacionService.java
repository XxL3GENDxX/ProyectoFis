package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.*;
import com.udistrital.gestionacademica.repositorio.*;
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
    private final PeriodoRepository periodoRepository;
    private final TokenUsuarioRepository tokenUsuarioRepository;
    private final ProfesorRepository profesorRepository;

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
     * Obtener calificaciones de un estudiante por período
     */
    @Transactional(readOnly = true)
    public List<Calificacion> obtenerCalificacionesPorEstudianteYPeriodo(Long codigoEstudiante, Long idPeriodo) {
        log.info("Obteniendo calificaciones del estudiante {} en periodo {}", codigoEstudiante, idPeriodo);
        
        // Verificar que el estudiante existe
        Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        // Verificar que el período existe
        Periodo periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new RuntimeException("Período no encontrado"));
        
        return calificacionRepository.findByEstudianteCodigoEstudianteAndPeriodo(codigoEstudiante, idPeriodo);
    }

    /**
     * Obtener calificaciones por nombre de período
     */
    @Transactional(readOnly = true)
    public List<Calificacion> obtenerCalificacionesPorPeriodo(String nombrePeriodo) {
        log.info("Obteniendo calificaciones del periodo: {}", nombrePeriodo);
        return calificacionRepository.findByPeriodoNombrePeriodo(nombrePeriodo);
    }

    /**
     * Asignar un logro a un estudiante en un período específico
     */
    public Calificacion asignarLogro(Long codigoEstudiante, Long idLogro, Long idPeriodo, String nombreUsuario) {
        log.info("Asignando logro {} al estudiante {} en periodo {}", idLogro, codigoEstudiante, idPeriodo);
        
        // Verificar que el estudiante existe
        Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        // Verificar que el logro existe
        Logro logro = logroRepository.findById(idLogro)
                .orElseThrow(() -> new RuntimeException("Logro no encontrado"));
        
        // Verificar que el período existe
        Periodo periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new RuntimeException("Período no encontrado"));
                
        // Buscar el profesor basado en el usuario logueado (opcional)
        TokenUsuario usuario = tokenUsuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + nombreUsuario));
                
        // Buscar la persona asociada
        Persona personaUsuario = usuario.getPersona();
        log.info("Buscando perfil de profesor para la persona ID: {} - Nombre: {} {}", 
                personaUsuario.getIdPersona(), personaUsuario.getNombre(), personaUsuario.getApellido());

        // Intentar encontrar el perfil de profesor, pero permitir que sea null
        Profesor profesor = profesorRepository.findByPersona(personaUsuario)
                .orElse(null);
        
        if (profesor != null) {
            log.info("Perfil de profesor encontrado. Asignando logro con profesor ID: {}", profesor.getIdProfesor());
        } else {
            log.warn("No se encontró perfil de profesor para el usuario '{}'. La calificación se creará sin profesor asociado.", nombreUsuario);
        }
        
        // Verificar que no se haya asignado ya este logro al estudiante en este período
        Optional<Calificacion> calificacionExistente = 
            calificacionRepository.findByEstudianteAndLogroPeriodo(codigoEstudiante, idLogro, idPeriodo);
        
        if (calificacionExistente.isPresent()) {
            throw new RuntimeException("Este logro ya ha sido asignado al estudiante en este período");
        }
        
        // Crear la nueva calificación
        Calificacion calificacion = new Calificacion();
        calificacion.setEstudiante(estudiante);
        calificacion.setLogro(logro);
        calificacion.setPeriodo(periodo);
        calificacion.setProfesor(profesor);
        
        Calificacion nuevaCalificacion = calificacionRepository.save(calificacion);
        log.info("Logro asignado exitosamente con ID: {}", nuevaCalificacion.getIdCalificacion());
        
        return nuevaCalificacion;
    }

    /**
     * Asignar un logro a un estudiante (sin especificar período, usa el actual)
     */
    public Calificacion asignarLogroConPeriodoActual(Long codigoEstudiante, Long idLogro, String nombreUsuario) {
        log.info("Asignando logro {} al estudiante {} (usando período actual)", idLogro, codigoEstudiante);
        
        // Obtener el período actual
        Periodo periodoActual = periodoRepository.findPeriodoActual()
                .orElseThrow(() -> new RuntimeException("No hay un período activo en la fecha actual"));
        
        return asignarLogro(codigoEstudiante, idLogro, periodoActual.getIdPeriodo(), nombreUsuario);
    }

    /**
     * Modificar una calificación (ya no modifica observaciones)
     */
    public Calificacion modificarCalificacion(Long idCalificacion) {
        log.info("Modificando calificación: {}", idCalificacion);
        
        Calificacion calificacion = calificacionRepository.findById(idCalificacion)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada"));
        
        // Aquí se pueden agregar otras modificaciones si el usuario lo requiere en el futuro
        // Por ahora, solo guardamos para mantener la estructura pero sin cambios reales
        
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
     * Obtener el historial de un estudiante filtrado por período
     */
    @Transactional(readOnly = true)
    public HistorialEstudiante obtenerHistorialPorPeriodo(Long codigoEstudiante, Long idPeriodo) {
        log.info("Obteniendo historial del estudiante {} en período {}", codigoEstudiante, idPeriodo);
        
        Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        List<Calificacion> calificaciones = 
            calificacionRepository.findByEstudianteCodigoEstudianteAndPeriodo(codigoEstudiante, idPeriodo);
        
        long totalLogros = calificacionRepository.countByEstudianteAndPeriodo(codigoEstudiante, idPeriodo);
        
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