package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Calificacion;
import com.udistrital.gestionacademica.servicio.CalificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calificaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CalificacionController {

    private final CalificacionService calificacionService;

    /**
     * Obtener calificaciones de un estudiante
     */
    @GetMapping("/estudiante/{codigoEstudiante}")
    public ResponseEntity<?> obtenerCalificacionesPorEstudiante(@PathVariable Long codigoEstudiante) {
        try {
            log.info("Obteniendo calificaciones del estudiante: {}", codigoEstudiante);
            
            List<Calificacion> calificaciones = 
                calificacionService.obtenerCalificacionesPorEstudiante(codigoEstudiante);
            
            return ResponseEntity.ok(calificaciones);
            
        } catch (RuntimeException e) {
            log.error("Error al obtener calificaciones: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener calificaciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener calificaciones de un estudiante por período
     */
    @GetMapping("/estudiante/{codigoEstudiante}/periodo/{idPeriodo}")
    public ResponseEntity<?> obtenerCalificacionesPorEstudiantePeriodo(
            @PathVariable Long codigoEstudiante,
            @PathVariable Long idPeriodo) {
        try {
            log.info("Obteniendo calificaciones del estudiante {} en periodo {}", codigoEstudiante, idPeriodo);
            
            List<Calificacion> calificaciones = 
                calificacionService.obtenerCalificacionesPorEstudianteYPeriodo(codigoEstudiante, idPeriodo);
            
            return ResponseEntity.ok(calificaciones);
            
        } catch (RuntimeException e) {
            log.error("Error al obtener calificaciones: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener calificaciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener calificaciones por nombre de período
     */
    @GetMapping("/periodo/{nombrePeriodo}")
    public ResponseEntity<?> obtenerCalificacionesPorPeriodo(@PathVariable String nombrePeriodo) {
        try {
            log.info("Obteniendo calificaciones del periodo: {}", nombrePeriodo);
            
            List<Calificacion> calificaciones = 
                calificacionService.obtenerCalificacionesPorPeriodo(nombrePeriodo);
            
            return ResponseEntity.ok(calificaciones);
            
        } catch (Exception e) {
            log.error("Error al obtener calificaciones: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Asignar logro a estudiante en un período específico
     */
    @PostMapping("/asignar")
    public ResponseEntity<?> asignarLogro(@RequestBody AsignarLogroRequest request) {
        try {
            log.info("Asignando logro {} al estudiante {} en periodo {}", 
                request.getIdLogro(), request.getCodigoEstudiante(), request.getIdPeriodo());
            
            Calificacion calificacion = calificacionService.asignarLogro(
                request.getCodigoEstudiante(),
                request.getIdLogro(),
                request.getIdPeriodo(),
                request.getNombreUsuario()
            );
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(calificacion);
                    
        } catch (RuntimeException e) {
            log.error("Error al asignar logro: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al asignar logro", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Asignar logro a estudiante usando el período actual
     */
    @PostMapping("/asignar/actual")
    public ResponseEntity<?> asignarLogroConPeriodoActual(@RequestBody AsignarLogroActualRequest request) {
        try {
            log.info("Asignando logro {} al estudiante {} (periodo actual)", 
                request.getIdLogro(), request.getCodigoEstudiante());
            
            Calificacion calificacion = calificacionService.asignarLogroConPeriodoActual(
                request.getCodigoEstudiante(),
                request.getIdLogro(),
                request.getNombreUsuario()
            );
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(calificacion);
                    
        } catch (RuntimeException e) {
            log.error("Error al asignar logro: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al asignar logro", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Modificar calificación
     */
    @PutMapping("/{idCalificacion}")
    public ResponseEntity<?> modificarCalificacion(
            @PathVariable Long idCalificacion,
            @RequestBody ModificarCalificacionRequest request) {
        try {
            log.info("Modificando calificación: {}", idCalificacion);
            
            Calificacion calificacion = calificacionService.modificarCalificacion(
                idCalificacion
            );
            
            return ResponseEntity.ok(calificacion);
                    
        } catch (RuntimeException e) {
            log.error("Error al modificar calificación: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al modificar calificación", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Eliminar calificación
     */
    @DeleteMapping("/{idCalificacion}")
    public ResponseEntity<?> eliminarCalificacion(@PathVariable Long idCalificacion) {
        try {
            log.info("Eliminando calificación: {}", idCalificacion);
            
            calificacionService.eliminarCalificacion(idCalificacion);
            
            return ResponseEntity.noContent().build();
                    
        } catch (RuntimeException e) {
            log.error("Error al eliminar calificación: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar calificación", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener historial completo del estudiante
     */
    @GetMapping("/historial/{codigoEstudiante}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable Long codigoEstudiante) {
        try {
            log.info("Obteniendo historial del estudiante: {}", codigoEstudiante);
            
            CalificacionService.HistorialEstudiante historial = 
                calificacionService.obtenerHistorial(codigoEstudiante);
            
            return ResponseEntity.ok(historial);
                    
        } catch (RuntimeException e) {
            log.error("Error al obtener historial: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener historial", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener historial del estudiante filtrado por período
     */
    @GetMapping("/historial/{codigoEstudiante}/periodo/{idPeriodo}")
    public ResponseEntity<?> obtenerHistorialPorPeriodo(
            @PathVariable Long codigoEstudiante,
            @PathVariable Long idPeriodo) {
        try {
            log.info("Obteniendo historial del estudiante {} en periodo {}", codigoEstudiante, idPeriodo);
            
            CalificacionService.HistorialEstudiante historial = 
                calificacionService.obtenerHistorialPorPeriodo(codigoEstudiante, idPeriodo);
            
            return ResponseEntity.ok(historial);
                    
        } catch (RuntimeException e) {
            log.error("Error al obtener historial: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener historial", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Crear respuesta de error
     */
    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("mensaje", mensaje);
        return response;
    }

    // ========== DTOs ==========
    
    @lombok.Data
    public static class AsignarLogroRequest {
        private Long codigoEstudiante;
        private Long idLogro;
        private Long idPeriodo;
        private String nombreUsuario;
    }

    @lombok.Data
    public static class AsignarLogroActualRequest {
        private Long codigoEstudiante;
        private Long idLogro;
        private String nombreUsuario;
    }

    @lombok.Data
    public static class ModificarCalificacionRequest {
    }
}