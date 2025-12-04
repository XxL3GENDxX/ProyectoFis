package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Citacion;
import com.udistrital.gestionacademica.servicio.CitacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CitacionController {

    private final CitacionService citacionService;

    /**
     * Crear citación individual
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearCitacion(@RequestBody CrearCitacionRequest request) {
        try {
            log.info("Creando citación para estudiante: {}", request.getCodigoEstudiante());

            Citacion citacion = citacionService.crearCitacion(
                    request.getCodigoEstudiante(),
                    request.getFechaCitacion()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(citacion);

        } catch (RuntimeException e) {
            log.error("Error al crear citación: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear citación", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Crear citaciones múltiples
     */
    @PostMapping("/crear-multiples")
    public ResponseEntity<?> crearCitacionesMultiples(@RequestBody CrearCitacionesMultiplesRequest request) {
        try {
            log.info("Creando citaciones para {} estudiantes", request.getCodigosEstudiantes().size());

            List<Citacion> citaciones = citacionService.crearCitacionesMultiples(
                    request.getCodigosEstudiantes(),
                    request.getFechaCitacion()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("error", false);
            response.put("mensaje", "Citaciones creadas exitosamente");
            response.put("citacionesCreadas", citaciones.size());
            response.put("citaciones", citaciones);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {
            log.error("Error al crear citaciones: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear citaciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener citaciones por acudiente
     */
    @GetMapping("/acudiente/{idAcudiente}")
    public ResponseEntity<?> obtenerCitacionesPorAcudiente(@PathVariable Long idAcudiente) {
        try {
            List<Citacion> citaciones = citacionService.obtenerCitacionesPorAcudiente(idAcudiente);
            return ResponseEntity.ok(citaciones);
        } catch (Exception e) {
            log.error("Error al obtener citaciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener citaciones por estudiante
     */
    @GetMapping("/estudiante/{codigoEstudiante}")
    public ResponseEntity<?> obtenerCitacionesPorEstudiante(@PathVariable Long codigoEstudiante) {
        try {
            List<Citacion> citaciones = citacionService.obtenerCitacionesPorEstudiante(codigoEstudiante);
            return ResponseEntity.ok(citaciones);
        } catch (Exception e) {
            log.error("Error al obtener citaciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener citaciones por grupo
     */
    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<?> obtenerCitacionesPorGrupo(@PathVariable Long idGrupo) {
        try {
            List<Citacion> citaciones = citacionService.obtenerCitacionesPorGrupo(idGrupo);
            return ResponseEntity.ok(citaciones);
        } catch (Exception e) {
            log.error("Error al obtener citaciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener todas las citaciones
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodasLasCitaciones() {
        try {
            List<Citacion> citaciones = citacionService.obtenerTodasLasCitaciones();
            return ResponseEntity.ok(citaciones);
        } catch (Exception e) {
            log.error("Error al obtener citaciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Eliminar citación
     */
    @DeleteMapping("/{idCitacion}")
    public ResponseEntity<?> eliminarCitacion(@PathVariable Long idCitacion) {
        try {
            citacionService.eliminarCitacion(idCitacion);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar citación: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar citación", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("mensaje", mensaje);
        return response;
    }

    // ========== CLASES INTERNAS PARA REQUEST ==========

    @lombok.Data
    public static class CrearCitacionRequest {
        private Long codigoEstudiante;
        private LocalDateTime fechaCitacion;
    }

    @lombok.Data
    public static class CrearCitacionesMultiplesRequest {
        private List<Long> codigosEstudiantes;
        private LocalDateTime fechaCitacion;
    }
}