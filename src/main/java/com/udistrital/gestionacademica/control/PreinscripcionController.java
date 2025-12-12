package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.modelo.Preinscripcion;
import com.udistrital.gestionacademica.servicio.EstudianteService;
import com.udistrital.gestionacademica.servicio.PreinscripcionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/preinscripcion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class PreinscripcionController {

    private final PreinscripcionService preinscripcionService;
    private final EstudianteService estudianteService;

    /**
     * Crear nueva preinscripción
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearPreinscripcion(@RequestBody Preinscripcion preinscripcion) {
        try {
            log.info("Creando preinscripción para estudiante: {}",
                    preinscripcion.getAspirante().getCodigoEstudiante());

            Preinscripcion nuevaPreinscripcion = preinscripcionService.crearPreinscripcion(preinscripcion);

            log.info("Preinscripción creada exitosamente con ID: {}", nuevaPreinscripcion.getIdPreinscripcion());

            return new ResponseEntity<>(nuevaPreinscripcion, HttpStatus.CREATED);

        } catch (Exception e) {
            log.error("Error al crear preinscripción: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error al crear preinscripción: " + e.getMessage()));
        }
    }

    /**
     * Obtener todas las preinscripciones
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodasLasPreinscripciones() {
        try {
            log.info("Obteniendo todas las preinscripciones");

            List<Preinscripcion> preinscripciones = preinscripcionService.obtenerTodasLasPreinscripciones();

            return ResponseEntity.ok(preinscripciones);

        } catch (Exception e) {
            log.error("Error al obtener preinscripciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Obtener preinscripción por ID
     */
    @GetMapping("/{idPreinscripcion}")
    public ResponseEntity<?> obtenerPreinscripcionPorId(@PathVariable Long idPreinscripcion) {
        try {
            log.info("Obteniendo preinscripción con ID: {}", idPreinscripcion);

            Preinscripcion preinscripcion = preinscripcionService.obtenerPreinscripcionPorId(idPreinscripcion);

            return ResponseEntity.ok(preinscripcion);

        } catch (RuntimeException e) {
            log.error("Preinscripción no encontrada: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError("Preinscripción no encontrada"));
        } catch (Exception e) {
            log.error("Error al obtener preinscripción", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Buscar preinscripciones por texto (nombre de aspirante o acudiente)
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPreinscripciones(@RequestParam String textoBusqueda) {
        try {
            log.info("Buscando preinscripciones con texto: {}", textoBusqueda);

            List<Preinscripcion> preinscripciones
                    = preinscripcionService.buscarPreinscripciones(textoBusqueda);

            if (preinscripciones.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(crearRespuestaError("No se encontraron preinscripciones"));
            }

            return ResponseEntity.ok(preinscripciones);

        } catch (Exception e) {
            log.error("Error al buscar preinscripciones", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Programar entrevista para una preinscripción
     */
    @PatchMapping("/{idPreinscripcion}/programar-entrevista")
    public ResponseEntity<?> programarEntrevista(
            @PathVariable Long idPreinscripcion,
            @RequestBody Map<String, String> datosEntrevista) {

        try {
            log.info("Programando entrevista para preinscripción: {}", idPreinscripcion);

            String fechaEntrevista = datosEntrevista.get("fechaEntrevista");
            String lugarEntrevista = datosEntrevista.get("lugarEntrevista");

            if (fechaEntrevista == null || lugarEntrevista == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(crearRespuestaError("Fecha y lugar de entrevista son obligatorios"));
            }

            Preinscripcion preinscripcionActualizada
                    = preinscripcionService.programarEntrevista(
                            idPreinscripcion,
                            fechaEntrevista,
                            lugarEntrevista
                    );

            return ResponseEntity.ok(crearRespuestaExito(
                    "Entrevista programada exitosamente",
                    preinscripcionActualizada
            ));

        } catch (RuntimeException e) {
            log.error("Error al programar entrevista: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al programar entrevista", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Cambiar estado del aspirante y actualizar hoja de vida
     */
    @PatchMapping("/{idPreinscripcion}/cambiar-estado")
    public ResponseEntity<?> cambiarEstadoAspirante(
            @PathVariable Long idPreinscripcion,
            @RequestBody Map<String, String> datosActualizacion) {

        try {
            log.info("Cambiando estado de aspirante para preinscripción: {}", idPreinscripcion);

            String nuevoEstado = datosActualizacion.get("nuevoEstado");

            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(crearRespuestaError("El nuevo estado es obligatorio"));
            }

            // Obtener preinscripción
            Preinscripcion preinscripcion
                    = preinscripcionService.obtenerPreinscripcionPorId(idPreinscripcion);

            // Actualizar información médica y del acudiente si se proporcionó
            if (datosActualizacion.containsKey("alergias")
                    || datosActualizacion.containsKey("enfermedades")
                    || datosActualizacion.containsKey("problemasAprendizaje")) {

                // Aquí deberías actualizar la información médica del estudiante
                // Por ahora solo logueamos
                log.info("Datos médicos actualizados: Alergias={}, Enfermedades={}, Problemas={}",
                        datosActualizacion.get("alergias"),
                        datosActualizacion.get("enfermedades"),
                        datosActualizacion.get("problemasAprendizaje"));
            }

            // Actualizar teléfono del acudiente si se proporcionó
            if (datosActualizacion.containsKey("telefonoAcudiente")) {
                String telefono = datosActualizacion.get("telefonoAcudiente");
                preinscripcionService.actualizarTelefonoAcudiente(
                        preinscripcion.getAcudiente().getIdAcudiente(),
                        telefono
                );
            }

            // Cambiar estado del estudiante
            Estudiante estudianteActualizado = estudianteService.cambiarEstadoEstudianteDirecto(
                    preinscripcion.getAspirante().getCodigoEstudiante(),
                    nuevoEstado
            );

            // Actualizar estado del acudiente si el estudiante fue aprobado
            if ("Aprobado".equalsIgnoreCase(nuevoEstado)) {
                preinscripcionService.actualizarEstadoAcudiente(
                        preinscripcion.getAcudiente().getIdAcudiente(),
                        "Activo"
                );
            }

            return ResponseEntity.ok(crearRespuestaExito(
                    "Estado actualizado exitosamente",
                    estudianteActualizado
            ));

        } catch (RuntimeException e) {
            log.error("Error al cambiar estado: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al cambiar estado", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    /**
     * Generar listado de aspirantes pendientes en PDF
     */
    @GetMapping("/generar-listado-pdf")
    public ResponseEntity<?> generarListadoPdf(
            @RequestParam(required = false) String estado) {

        try {
            log.info("Generando listado PDF de preinscripciones");

            // Esta funcionalidad se puede implementar similar al listado de estudiantes
            // Por ahora retornamos un mensaje indicando que está en desarrollo
            return ResponseEntity.ok(crearRespuestaExito(
                    "Funcionalidad en desarrollo",
                    null
            ));

        } catch (Exception e) {
            log.error("Error al generar PDF", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error al generar PDF"));
        }
    }

    // ========== Métodos auxiliares ==========
    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("mensaje", mensaje);
        return response;
    }

    private Map<String, Object> crearRespuestaExito(String mensaje, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("mensaje", mensaje);
        response.put("data", data);
        return response;
    }
}
