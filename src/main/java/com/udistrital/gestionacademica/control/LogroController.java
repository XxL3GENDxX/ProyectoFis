package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Logro;
import com.udistrital.gestionacademica.servicio.LogroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logros")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LogroController {

    private final LogroService logroService;

    /**
     * Obtener logros por categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<?> obtenerLogrosPorCategoria(@PathVariable String categoria) {
        try {
            log.info("Obteniendo logros de la categoría: {}", categoria);
            
            List<Logro> logros = logroService.obtenerLogrosPorCategoria(categoria);
            
            // Devolver 200 OK incluso si la lista está vacía (más RESTful)
            return ResponseEntity.ok(logros);
            
        } catch (Exception e) {
            log.error("Error al obtener logros de la categoría", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener un logro por ID
     */
    @GetMapping("/{idLogro}")
    public ResponseEntity<?> obtenerLogroPorId(@PathVariable Long idLogro) {
        try {
            log.info("Obteniendo logro con ID: {}", idLogro);
            
            Logro logro = logroService.obtenerLogroPorId(idLogro);
            return ResponseEntity.ok(logro);
            
        } catch (RuntimeException e) {
            log.error("Error al obtener logro: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError("Logro no encontrado"));
        } catch (Exception e) {
            log.error("Error al obtener logro", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Crear un nuevo logro
     */
    @PostMapping
    public ResponseEntity<?> crearLogro(@RequestBody Logro logro) {
        try {
            log.info("Creando nuevo logro: {}", logro.getNombreLogro());
            
            Logro nuevoLogro = logroService.crearLogro(logro);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(nuevoLogro);
                    
        } catch (RuntimeException e) {
            log.error("Error al crear logro: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear logro", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Actualizar un logro existente
     */
    @PutMapping("/{idLogro}")
    public ResponseEntity<?> actualizarLogro(
            @PathVariable Long idLogro,
            @RequestBody Logro logro) {
        try {
            log.info("Actualizando logro con ID: {}", idLogro);
            
            Logro logroActualizado = logroService.actualizarLogro(idLogro, logro);
            
            return ResponseEntity.ok(logroActualizado);
                    
        } catch (RuntimeException e) {
            log.error("Error al actualizar logro: {}", e.getMessage());
            
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(crearRespuestaError(e.getMessage()));
            }
            
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar logro", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Eliminar un logro
     */
    @DeleteMapping("/{idLogro}")
    public ResponseEntity<?> eliminarLogro(@PathVariable Long idLogro) {
        try {
            log.info("Eliminando logro con ID: {}", idLogro);
            
            logroService.eliminarLogro(idLogro);
            
            return ResponseEntity.noContent().build();
                    
        } catch (RuntimeException e) {
            log.error("Error al eliminar logro: {}", e.getMessage());
            
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(crearRespuestaError(e.getMessage()));
            }
            
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar logro", e);
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
}