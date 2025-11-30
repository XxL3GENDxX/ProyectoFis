package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Logro;
import com.udistrital.gestionacademica.servicio.LogroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logro")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://127.0.0.1:8080","http://localhost:8080"})
public class LogroController {

    private final LogroService logroService;

    /**
     * GET /api/logro/categorias
     * Obtiene todas las categorías de logros
     */
    @GetMapping("/categorias")
    public ResponseEntity<?> obtenerCategorias() {
        try {
            log.info("GET /api/logro/categorias");
            List<String> categorias = logroService.obtenerCategorias();
            return new ResponseEntity<>(categorias, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener categorías: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al obtener categorías: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * GET /api/logro/categoria/{categoria}
     * Obtiene los logros de una categoría específica
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<?> obtenerLogrosPorCategoria(@PathVariable String categoria) {
        try {
            log.info("GET /api/logro/categoria/{}", categoria);
            List<Logro> logros = logroService.obtenerLogrosPorCategoria(categoria);
            return new ResponseEntity<>(logros, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener logros: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al obtener logros: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * POST /api/logro/crear
     * Crea un nuevo logro
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearLogro(@RequestBody Logro logro) {
        try {
            log.info("POST /api/logro/crear - Nombre: {}, Categoría: {}", logro.getNombreLogro(), logro.getCategoriaLogro());
            
            Logro nuevoLogro = logroService.crearLogro(logro);
            return new ResponseEntity<>(nuevoLogro, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validación fallida al crear logro: {}", e.getMessage());
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error al crear logro: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al crear logro: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * GET /api/logro/{id}
     * Obtiene un logro específico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerLogro(@PathVariable Long id) {
        try {
            log.info("GET /api/logro/{}", id);
            Logro logro = logroService.obtenerLogro(id);
            return new ResponseEntity<>(logro, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Logro no encontrado: {}", e.getMessage());
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al obtener logro: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al obtener logro: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * PUT /api/logro/{id}
     * Edita un logro existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editarLogro(@PathVariable Long id, @RequestBody Logro logroActualizado) {
        try {
            log.info("PUT /api/logro/{} - Nombre: {}", id, logroActualizado.getNombreLogro());
            
            Logro logro = logroService.editarLogro(id, logroActualizado);
            return new ResponseEntity<>(logro, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            log.warn("Error al editar logro: {}", e.getMessage());
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al editar logro: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al editar logro: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * PATCH /api/logro/{id}/cambiar-estado
     * Cambia el estado (habilita/inhabilita) un logro
     */
    @PatchMapping("/{id}/cambiar-estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id) {
        try {
            log.info("PATCH /api/logro/{}/cambiar-estado", id);
            
            Logro logro = logroService.cambiarEstado(id);
            return new ResponseEntity<>(logro, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            log.warn("Logro no encontrado: {}", e.getMessage());
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al cambiar estado: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al cambiar estado: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * DELETE /api/logro/{id}
     * Elimina un logro (opcional)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarLogro(@PathVariable Long id) {
        try {
            log.info("DELETE /api/logro/{}", id);
            
            logroService.eliminarLogro(id);
            return new ResponseEntity<>(
                Map.of("mensaje", "Logro eliminado exitosamente"),
                HttpStatus.OK
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("Logro no encontrado: {}", e.getMessage());
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al eliminar logro: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al eliminar logro: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

}
